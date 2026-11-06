package com.ncgr.search;

import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.object.deployer.ObjectDefinitionDeployer;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Registers the contributor with both indexers Liferay uses for object entries:
 *
 * <ul>
 * <li>
 * The indexer for the definition's dynamic class name (for example
 * "com.liferay.object.model.ObjectDefinition#W6I6"), which writes an entry when
 * it is saved. Liferay calls this deployer once per object definition.
 * </li>
 * <li>
 * The global indexer for the ObjectEntry class name, which writes every entry
 * during a full reindex. The per definition indexers do not run in that mode.
 * </li>
 * </ul>
 *
 * @author mumentayyem
 */
@Component(service = ObjectDefinitionDeployer.class)
public class ObjectEntryFlatFieldsObjectDefinitionDeployer
	implements ObjectDefinitionDeployer {

	@Override
	public List<ServiceRegistration<?>> deploy(
		ObjectDefinition objectDefinition) {

		if (objectDefinition.isSystem()) {
			return Collections.emptyList();
		}

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put(
			"indexer.class.name", objectDefinition.getClassName());

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Registering flat fields contributor for " +
					objectDefinition.getClassName());
		}

		return Collections.singletonList(
			_bundleContext.registerService(
				ModelDocumentContributor.class,
				new ObjectEntryFlatFieldsContributor(
					_dlFileEntryLocalService, _objectFieldLocalService),
				properties));
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("indexer.class.name", ObjectEntry.class.getName());

		_serviceRegistration = _bundleContext.registerService(
			ModelDocumentContributor.class,
			new ObjectEntryFlatFieldsContributor(
				_dlFileEntryLocalService, _objectFieldLocalService),
			properties);
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectEntryFlatFieldsObjectDefinitionDeployer.class);

	private BundleContext _bundleContext;

	@Reference
	private DLFileEntryLocalService _dlFileEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	private ServiceRegistration<ModelDocumentContributor> _serviceRegistration;

}
