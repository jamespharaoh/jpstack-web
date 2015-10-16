package wbs.platform.service.model;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class ServiceObjectHelperImplementation
	implements ServiceObjectHelperMethods {

	@Inject
	Provider<ObjectManager> objectManagerProvider;

	@Inject
	Provider<ServiceObjectHelper> serviceHelperProvider;

	@Inject
	Provider<ServiceTypeObjectHelper> serviceTypeHelperProvider;

	@Inject
	Provider<ObjectTypeObjectHelper> objectTypeHelperProvider;

	@Override
	public
	ServiceRec findOrCreate (
			Record<?> parent,
			String typeCode,
			String code) {

		ObjectManager objectManager =
			objectManagerProvider.get ();

		ObjectTypeObjectHelper objectTypeHelper =
			objectTypeHelperProvider.get ();

		ServiceObjectHelper serviceHelper =
			serviceHelperProvider.get ();

		ServiceTypeObjectHelper serviceTypeHelper =
			serviceTypeHelperProvider.get ();

		// lookup existing service...

		ServiceRec service =
			serviceHelper
				.findByCode (
					parent,
					code);

		if (service != null)
			return service;

		// ...or create new service

		ObjectTypeRec parentType =
			objectTypeHelper.find (
				objectManager.getObjectTypeId (parent));

		ServiceTypeRec serviceType =
			serviceTypeHelper.findByCode (
				parentType,
				typeCode);

		service =
			serviceHelper.insert (
				new ServiceRec ()

			.setCode (
				code)

			.setDescription (
				serviceType.getDescription ())

			.setServiceType (
				serviceType)

			.setParentType (
				parentType)

			.setParentId (
				parent.getId ())

		);

		return service;

	}

}