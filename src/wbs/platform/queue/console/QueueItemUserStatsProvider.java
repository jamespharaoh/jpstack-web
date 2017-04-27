package wbs.platform.queue.console;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.scaffold.model.SliceObjectHelper;

@SingletonComponent ("queueItemUserStatsProvider")
public
class QueueItemUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueItemObjectHelper queueItemHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <QueueStatsFilter> queueStatsFilterProvider;

	// implementation

	@Override
	public
	StatsDataSet getStats (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull StatsPeriod statsPeriod,
			@NonNull Map <String, Object> conditions) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getStats");

		) {

			if (statsPeriod.granularity () != StatsGranularity.hour)
				throw new IllegalArgumentException ();

			// setup data structures

			Map <Long, long[]> numProcessedPerUser =
				new TreeMap<> ();

			Set <Object> userIdObjects =
				new HashSet<> ();

			// retrieve queue items

			QueueStatsFilter queueStatsFilter =
				queueStatsFilterProvider.get ();

			queueStatsFilter.conditions (
				conditions);

			List <QueueItemRec> queueItems =
				queueStatsFilter.filterQueueItems (
					queueItemHelper.findByProcessedTime (
						statsPeriod.toInterval ()));

			// aggregate stats

			for (
				QueueItemRec queueItem
					: queueItems
			) {

				QueueSubjectRec queueSubject =
					queueItem.getQueueSubject ();

				// TODO fix data!
				QueueRec queue =
					queueSubject != null
						? queueSubject.getQueue ()
						: queueItem.getQueue ();

				Record <?> parent =
					objectManager.getParentRequired (
						queue);

				if (
					! privChecker.canRecursive (
						taskLogger,
						parent,
						"supervisor")
				) {
					continue;
				}

				int hour =
					statsPeriod.assign (
						queueItem.getProcessedTime ());


				if (! userIdObjects.contains (
						queueItem.getProcessedUser ().getId ())) {

					userIdObjects.add (
						queueItem.getProcessedUser ().getId ());

					numProcessedPerUser.put (
						queueItem.getProcessedUser ().getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

				}

				long[] numProcessedForUser =
					numProcessedPerUser.get (
						queueItem.getProcessedUser ().getId ());

				numProcessedForUser [hour] ++;

			}

			// create return value

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			statsDataSet.indexValues ().put (
				"userId",
				userIdObjects);

			for (
				int hour = 0;
				hour < statsPeriod.size ();
				hour ++
			) {

				for (
					Object userIdObject
						: userIdObjects
				) {

					Long userId =
						(Long)
						userIdObject;

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							statsPeriod.step (
								hour))

						.addIndex (
							"userId",
							userId)

						.addValue (
							"numProcessed",
							numProcessedPerUser.get (userId) [hour]));

				}

			}

			return statsDataSet;

		}

	}

}
