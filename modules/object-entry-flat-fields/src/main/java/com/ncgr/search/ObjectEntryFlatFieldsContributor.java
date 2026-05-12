package com.ncgr.search;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author mumentayyem
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntry",
	service = ModelDocumentContributor.class
)
public class ObjectEntryFlatFieldsContributor
	implements ModelDocumentContributor<ObjectEntry> {

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		try {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.getObjectDefinition(
					objectEntry.getObjectDefinitionId());

			if (objectDefinition.isSystem()) {
				return;
			}

			Map<String, Serializable> values = objectEntry.getValues();

			List<ObjectField> objectFields =
				_objectFieldLocalService.getObjectFields(
					objectDefinition.getObjectDefinitionId());

			for (ObjectField objectField : objectFields) {
				Serializable value = values.get(objectField.getName());

				if (value == null) {
					continue;
				}

				String key = "custom_" + objectField.getName();

				if (value instanceof Number) {
					document.addNumber(key, value.toString());
				}
				else {
					document.addKeyword(key, String.valueOf(value));
				}
			}
		}
		catch (Exception exception) {
			_log.warn("Could not enrich ObjectEntry document", exception);
		}
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryFlatFieldsContributor.class);

}
