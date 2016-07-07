package wbs.platform.service.model;

import static wbs.framework.utils.etc.Misc.isPresent;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.scaffold.model.SliceRec;

public
class ServiceObjectHelperImplementation
	implements ServiceObjectHelperMethods {

	// dependencies

	@Inject
	Provider<ObjectManager> objectManagerProvider;

	@Inject
	Provider<ServiceObjectHelper> serviceHelperProvider;

	@Inject
	Provider<ServiceTypeObjectHelper> serviceTypeHelperProvider;

	@Inject
	Provider<ObjectTypeObjectHelper> objectTypeHelperProvider;

	// implementation

	@Override
	public
	ServiceRec findOrCreate (
			@NonNull Record<?> parent,
			@NonNull String typeCode,
			@NonNull String code) {

		ObjectManager objectManager =
			objectManagerProvider.get ();

		ObjectTypeObjectHelper objectTypeHelper =
			objectTypeHelperProvider.get ();

		ServiceObjectHelper serviceHelper =
			serviceHelperProvider.get ();

		ServiceTypeObjectHelper serviceTypeHelper =
			serviceTypeHelperProvider.get ();

		// lookup existing service

		Optional<ServiceRec> existingService =
			serviceHelper.findByCode (
				parent,
				code);

		if (
			isPresent (
				existingService)
		) {
			return existingService.get ();
		}

		// create new service

		ObjectTypeRec parentType =
			objectTypeHelper.findRequired (
				objectManager.getObjectTypeId (
					parent));

		ServiceTypeRec serviceType =
			serviceTypeHelper.findByCodeRequired (
				parentType,
				typeCode);

		Optional<SliceRec> parentSlice =
			objectManager.getAncestor (
				SliceRec.class,
				parent);

		ServiceRec newService =
			serviceHelper.insert (
				serviceHelper.createInstance ()

			.setCode (
				code)

			.setServiceType (
				serviceType)

			.setParentType (
				parentType)

			.setParentId (
				parent.getId ())

			.setSlice (
				parentSlice.orNull ())

		);

		return newService;

	}

}