package wbs.platform.queue.model;

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
class QueueHooks
	implements ObjectHooks<QueueRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	QueueTypeDao queueTypeDao;

	// indirect dependencies

	@Inject
	Provider<ObjectManager> objectManagerProvider;

	// state

	Map<Long,List<Long>> queueTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"queueTypeHooks.init ()",
				this);

		// preload object types

		objectTypeDao.findAll ();

		// load queue types and construct index

		queueTypeIdsByParentTypeId =
			queueTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (
					queueType -> (long)
						queueType.getParentType ().getId (),
					Collectors.mapping (
						queueType -> (long)
							queueType.getId (),
						Collectors.toList ())));

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<QueueRec> queueHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				queueTypeIdsByParentTypeId.keySet (),
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
			Long queueTypeId
				: queueTypeIdsByParentTypeId.get (
					(long) parentHelper.objectTypeId ())
		) {

			QueueTypeRec queueType =
				queueTypeDao.findRequired (
					queueTypeId);

			queueHelper.insert (
				queueHelper.createInstance ()

				.setQueueType (
					queueType)

				.setCode (
					queueType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

				.setSlice (
					slice.orNull ())

				.setDefaultPriority (
					queueType.getDefaultPriority ())

			);


		}

	}

}