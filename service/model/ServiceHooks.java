package wbs.platform.service.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

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

	Map<Long,List<Long>> serviceTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"serviceHooks.init ()",
				this);

		// preload object types

		objectTypeDao.findAll ();

		// load service types and construct index

		serviceTypeIdsByParentTypeId =
			serviceTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (
					serviceType -> (long)
						serviceType.getParentType ().getId (),
					Collectors.mapping (
						serviceType -> (long)
							serviceType.getId (),
						Collectors.toList ())));

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<ServiceRec> serviceHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				serviceTypeIdsByParentTypeId.keySet (),
				(long) parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectManager objectManager =
	       objectManagerProvider.get ();

		Optional<SliceRec> slice =
			objectManager.getAncestor (
				SliceRec.class,
				parent);

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long serviceTypeId
				: serviceTypeIdsByParentTypeId.get (
					(long) parentHelper.objectTypeId ())
		) {

			ServiceTypeRec serviceType =
				serviceTypeDao.findRequired (
					serviceTypeId);

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