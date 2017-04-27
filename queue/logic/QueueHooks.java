package wbs.platform.queue.logic;

import static wbs.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.framework.object.ObjectManager;

import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueTypeDao;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.platform.scaffold.model.SliceRec;

public
class QueueHooks
	implements ObjectHooks <QueueRec> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ObjectTypeDao objectTypeDao;

	@SingletonDependency
	QueueTypeDao queueTypeDao;

	// state

	Map <Long, List <Long>> queueTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

			OwnedTransaction transaction =
				database.beginReadOnly (
					taskLogger,
					"queueTypeHooks.setup ()",
					this);

		) {

			// preload object types

			objectTypeDao.findAll ();

			// load queue types and construct index

			queueTypeIdsByParentTypeId =
				queueTypeDao.findAll ().stream ()

				.collect (
					Collectors.groupingBy (

					queueType ->
						queueType.getParentType ().getId (),

					Collectors.mapping (
						queueType ->
							queueType.getId (),
						Collectors.toList ()))

				);

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper <QueueRec> queueHelper,
			@NonNull ObjectHelper <?> parentHelper,
			@NonNull Record <?> parent) {

		if (
			doesNotContain (
				queueTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createSingletons");

		) {

			Optional <SliceRec> slice =
				objectManager.getAncestor (
					SliceRec.class,
					parent);

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			for (
				Long queueTypeId
					: queueTypeIdsByParentTypeId.get (
						parentHelper.objectTypeId ())
			) {

				QueueTypeRec queueType =
					queueTypeDao.findRequired (
						queueTypeId);

				queueHelper.insert (
					taskLogger,
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

}