package com.ncgr.search;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Flattens custom object field values onto the search document as top level
 * "custom_" fields. This is not an OSGi component: it is registered by {@link
 * ObjectEntryFlatFieldsObjectDefinitionDeployer}.
 *
 * @author mumentayyem
 */
public class ObjectEntryFlatFieldsContributor
	implements ModelDocumentContributor<ObjectEntry> {

	public ObjectEntryFlatFieldsContributor(
		DLFileEntryLocalService dlFileEntryLocalService,
		ObjectFieldLocalService objectFieldLocalService) {

		_dlFileEntryLocalService = dlFileEntryLocalService;
		_objectFieldLocalService = objectFieldLocalService;
	}

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		try {
			ObjectDefinition objectDefinition =
				objectEntry.getObjectDefinition();

			if (objectDefinition.isSystem()) {
				return;
			}

			Map<String, Serializable> values = objectEntry.getIndexedValues();

			if (MapUtil.isEmpty(values)) {
				return;
			}

			List<ObjectField> objectFields =
				_objectFieldLocalService.getObjectFields(
					objectDefinition.getObjectDefinitionId());

			for (ObjectField objectField : objectFields) {
				if (!objectField.isIndexed()) {
					continue;
				}

				if (objectField.compareBusinessType(
						ObjectFieldConstants.BUSINESS_TYPE_ATTACHMENT)) {

					_contributeAttachment(
						document, _getKey(objectField.getName()),
						values.get(objectField.getName()));
				}
				else if (objectField.isLocalized()) {
					_contributeLocalized(document, objectField, values);
				}
				else {
					_contribute(
						document, _getKey(objectField.getName()),
						values.get(objectField.getName()));
				}
			}
		}
		catch (Exception exception) {
			_log.warn("Could not enrich ObjectEntry document", exception);
		}
	}

	private void _contribute(Document document, String key, Object value) {
		if (value == null) {
			return;
		}

		if (value instanceof Boolean) {
			document.addKeyword(key, (Boolean)value);
		}
		else if (value instanceof Date) {
			document.addDate(key, (Date)value);
		}
		else if (value instanceof BigDecimal) {
			document.addNumber(key, ((BigDecimal)value).doubleValue());
		}
		else if (value instanceof Double) {
			document.addNumber(key, (Double)value);
		}
		else if (value instanceof Float) {
			document.addNumber(key, ((Float)value).doubleValue());
		}
		else if (value instanceof Integer) {
			document.addNumber(key, (Integer)value);
		}
		else if (value instanceof Long) {
			document.addNumber(key, (Long)value);
		}
		else if (value instanceof Short) {
			document.addNumber(key, ((Short)value).intValue());
		}
		else {
			String stringValue = String.valueOf(value);

			document.addKeyword(key, stringValue);
			document.addText(key.concat("_text"), stringValue);
		}
	}

	private void _contributeAttachment(
		Document document, String key, Object value) {

		long fileEntryId = GetterUtil.getLong(value);

		if (fileEntryId <= 0) {
			return;
		}

		document.addNumber(key.concat("_id"), fileEntryId);

		DLFileEntry dlFileEntry = _dlFileEntryLocalService.fetchDLFileEntry(
			fileEntryId);

		if (dlFileEntry != null) {
			_contribute(document, key, dlFileEntry.getFileName());
		}
	}

	private void _contributeLocalized(
		Document document, ObjectField objectField,
		Map<String, Serializable> values) {

		Serializable value = values.get(objectField.getI18nObjectFieldName());

		if (!(value instanceof Map)) {
			_contribute(document, _getKey(objectField.getName()), value);

			return;
		}

		Map<?, ?> localizedValues = (Map<?, ?>)value;

		for (Map.Entry<?, ?> entry : localizedValues.entrySet()) {
			_contribute(
				document,
				_getKey(objectField.getName()) + "_" +
					String.valueOf(entry.getKey()),
				entry.getValue());
		}
	}

	private String _getKey(String objectFieldName) {
		return _PREFIX.concat(objectFieldName);
	}

	private static final String _PREFIX = "custom_";

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryFlatFieldsContributor.class);

	private final DLFileEntryLocalService _dlFileEntryLocalService;
	private final ObjectFieldLocalService _objectFieldLocalService;

}
