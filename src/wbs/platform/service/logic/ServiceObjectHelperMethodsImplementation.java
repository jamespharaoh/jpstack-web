package wbs.platform.service.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

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

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ObjectManager objectManager;

	@WeakSingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@WeakSingletonDependency
	ServiceObjectHelper serviceHelper;

	@WeakSingletonDependency
	ServiceTypeObjectHelper serviceTypeHelper;

	// implementation

	@Override
	public
	ServiceRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record<?> parent,
			@NonNull String typeCode,
			@NonNull String code) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

		// lookup existing service

		Optional <ServiceRec> existingService =
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

		Optional <SliceRec> parentSlice =
			objectManager.getAncestor (
				SliceRec.class,
				parent);

		ServiceRec newService =
			serviceHelper.insert (
				taskLogger,
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