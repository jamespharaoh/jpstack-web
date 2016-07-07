package wbs.platform.service.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import com.google.common.base.Optional;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.scaffold.model.SliceRec;

public
class ServiceHooks
	implements ObjectHooks<ServiceRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	ServiceTypeDao serviceTypeDao;

	// indirect dependencies

	@Inject
	Provider<ObjectManager> objectManagerProvider;

	// state

	Set<Integer> parentObjectTypeIds =
		new HashSet<Integer> ();

	// lifecycle

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
				serviceTypeDao.findByParentType (
					objectType);

			if (serviceTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			ObjectHelper<ServiceRec> serviceHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (
			doesNotContain (
				parentObjectTypeIds,
				parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectManager objectManager =
			objectManagerProvider.get ();

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		Optional<SliceRec> slice =
			objectManager.getAncestor (
				SliceRec.class,
				parent);

		List<ServiceTypeRec> serviceTypes =
			serviceTypeDao.findByParentType (
				parentType);

		for (
			ServiceTypeRec serviceType
				: serviceTypes
		) {

			serviceHelper.insert (
				serviceHelper.createInstance ()

				.setServiceType (
					serviceType)

				.setCode (
					serviceType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

				.setSlice (
					slice.orNull ())

			);

		}

	}

}