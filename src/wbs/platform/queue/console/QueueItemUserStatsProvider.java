package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.priv.PrivChecker;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.scaffold.model.SliceObjectHelper;

@SingletonComponent ("queueItemUserStatsProvider")
public
class QueueItemUserStatsProvider
	implements StatsProvider {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@Inject
	Provider<QueueStatsFilter> queueStatsFilterProvider;

	// implementation

	@Override
	public
	StatsDataSet getStats (
			@NonNull StatsPeriod statsPeriod,
			@NonNull Map<String,Object> conditions) {

		if (statsPeriod.granularity () != StatsGranularity.hour)
			throw new IllegalArgumentException ();

		// setup data structures

		Map<Integer,int[]> numProcessedPerUser =
			new TreeMap<Integer,int[]> ();

		Set<Object> userIds =
			new HashSet<Object> ();

		// retrieve queue items

		QueueStatsFilter queueStatsFilter =
			queueStatsFilterProvider.get ();

		queueStatsFilter.conditions (
			conditions);

		List<QueueItemRec> queueItems =
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

			Record<?> parent =
				objectManager.getParent (queue);

			if (! privChecker.can (
					parent,
					"supervisor"))
				continue;

			int hour =
				statsPeriod.assign (
					dateToInstant (
						queueItem.getProcessedTime ()));


			if (! userIds.contains (
					queueItem.getProcessedUser ().getId ())) {

				userIds.add (
					queueItem.getProcessedUser ().getId ());

				numProcessedPerUser.put (
					queueItem.getProcessedUser ().getId (),
					new int [statsPeriod.size ()]);

			}

			int[] numProcessedForUser =
				numProcessedPerUser.get (
					queueItem.getProcessedUser ().getId ());

			numProcessedForUser [hour] ++;

		}

		// create return value

		StatsDataSet statsDataSet =
			new StatsDataSet ();

		statsDataSet.indexValues ()
			.put ("userId", userIds);

		for (
			int hour = 0;
			hour < statsPeriod.size ();
			hour ++
		) {

			for (Object userIdObject
					: userIds) {

				Integer userId =
					(Integer) userIdObject;

				statsDataSet.data ().add (
					new StatsDatum ()

					.startTime (
						statsPeriod.step (hour))

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
