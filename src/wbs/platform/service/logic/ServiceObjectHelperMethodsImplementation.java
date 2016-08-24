package wbs.platform.service.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceObjectHelperMethods;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.service.model.ServiceTypeObjectHelper;
import wbs.platform.service.model.ServiceTypeRec;

public
class ServiceObjectHelperMethodsImplementation
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
			optionalIsPresent (
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