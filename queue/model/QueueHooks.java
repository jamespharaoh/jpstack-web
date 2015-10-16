package wbs.platform.queue.model;

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
class QueueHooks
	extends AbstractObjectHooks<QueueRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	QueueTypeDao queueTypeDao;

	// state

	Set<Integer> parentObjectTypeIds =
		new HashSet<Integer> ();

	// init

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<ObjectTypeRec> objectTypes =
			objectTypeDao.findAll ();

		for (
			ObjectTypeRec objectType
				: objectTypes
		) {

			List<QueueTypeRec> queueTypes =
				queueTypeDao.findByParentObjectType (
					objectType);

			if (queueTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	@Override
	public
	void createSingletons (
			ObjectHelper<QueueRec> queueHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (! parentObjectTypeIds.contains (
				parentHelper.objectTypeId ()))
			return;

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<QueueTypeRec> queueTypes =
			queueTypeDao.findByParentObjectType (
				parentType);

		for (
			QueueTypeRec queueType
				: queueTypes
		) {

			queueHelper.insert (
				new QueueRec ()

				.setQueueType (
					queueType)

				.setCode (
					queueType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);


		}

	}

}