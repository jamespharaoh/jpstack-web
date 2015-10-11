package wbs.platform.service.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class ServiceHooks
	extends AbstractObjectHooks<ServiceRec> {

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	ServiceTypeDao serviceTypeDao;

	Set<Integer> parentObjectTypeIds =
		new HashSet<Integer> ();

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<ObjectTypeRec> objectTypes =
			objectTypeDao.findAll ();

		for (ObjectTypeRec objectType
				: objectTypes) {

			List<ServiceTypeRec> serviceTypes =
				serviceTypeDao.findByParentObjectType (
					objectType);

			if (serviceTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	@Override
	public
	void createSingletons (
			ObjectHelper<ServiceRec> serviceHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (! parentObjectTypeIds.contains (
				parentHelper.objectTypeId ()))
			return;

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<ServiceTypeRec> serviceTypes =
			serviceTypeDao.findByParentObjectType (
				parentType);

		for (ServiceTypeRec serviceType
				: serviceTypes) {

			serviceHelper.insert (
				new ServiceRec ()
					.setType (serviceType)
					.setCode (serviceType.getCode ())
					.setDescription (serviceType.getDescription ())
					.setParentObjectType (parentType)
					.setParentObjectId (parent.getId ()));

		}

	}

}