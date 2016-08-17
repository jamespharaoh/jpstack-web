package wbs.platform.queue.console;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
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

@SingletonComponent ("queueItemQueueStatsProvider")
public
class QueueItemQueueStatsProvider
	implements StatsProvider {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	QueueItemObjectHelper queueItemHelper;

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

		Map<Long,long[]> numProcessedPerQueue =
			new TreeMap<> ();

		Map<Long,long[]> numCreatedPerQueue =
			new TreeMap<> ();

		Map<Long,long[]> numPreferredPerQueue =
			new TreeMap<> ();

		Map<Long,long[]> numNotPreferredPerQueue =
			new TreeMap<> ();

		Set<Object> queueIds =
			new HashSet<> ();

		// retrieve queue items

		QueueStatsFilter queueStatsFilter =
			queueStatsFilterProvider.get ();

		queueStatsFilter.conditions (
			conditions);

		List<QueueItemRec> createdQueueItems =
			queueStatsFilter.filterQueueItems (
				queueItemHelper.findByCreatedTime (
					statsPeriod.toInterval ()));

		List<QueueItemRec> processedQueueItems =
			queueStatsFilter.filterQueueItems (
				queueItemHelper.findByProcessedTime (
					statsPeriod.toInterval ()));

		// aggregate created items

		for (
			QueueItemRec queueItem
				: createdQueueItems
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

			if (! privChecker.canRecursive (
					parent,
					"supervisor"))
				continue;

			int hour =
				statsPeriod.assign (
					queueItem.getCreatedTime ());

			if (! queueIds.contains (
					queue.getId ())) {

				queueIds.add (
					queue.getId ());

				numCreatedPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

				numProcessedPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

				numPreferredPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

				numNotPreferredPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

			}

			long[] numCreatedForQueue =
				numCreatedPerQueue.get (
					queue.getId ());

			numCreatedForQueue [hour] ++;

		}

		// aggregate processed items

		for (
			QueueItemRec queueItem
				: processedQueueItems
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

			if (! privChecker.canRecursive (
					parent,
					"supervisor"))
				continue;

			int hour =
				statsPeriod.assign (
					queueItem.getProcessedTime ());

			if (! queueIds.contains (
					queue.getId ())) {

				queueIds.add (
					queue.getId ());

				numCreatedPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

				numProcessedPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

				numPreferredPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

				numNotPreferredPerQueue.put (
					queue.getId (),
					new long [
						statsPeriod.size ()]);

			}

			long[] numProcessedForQueue =
				numProcessedPerQueue.get (
					queue.getId ());

			numProcessedForQueue [hour] ++;

			if (queueItem.getProcessedByPreferredUser () != null) {

				if (queueItem.getProcessedByPreferredUser ()) {

					long[] numPreferredForQueue =
						numPreferredPerQueue.get (
							queue.getId ());

					numPreferredForQueue [hour] ++;

				} else {

					long[] numNotPreferredForQueue =
						numNotPreferredPerQueue.get (
							queue.getId ());

					numNotPreferredForQueue [hour] ++;

				}

			}

		}

		// create return value

		StatsDataSet statsDataSet =
			new StatsDataSet ();

		statsDataSet.indexValues ()
			.put ("queueId", queueIds);

		for (
			int hour = 0;
			hour < statsPeriod.size ();
			hour ++
		) {

			for (Object queueIdObject
					: queueIds) {

				Integer queueId =
					(Integer) queueIdObject;

				statsDataSet.data ().add (
					new StatsDatum ()

					.startTime (
						statsPeriod.step (hour))

					.addIndex (
						"queueId",
						queueId)

					.addValue (
						"numCreated",
						numCreatedPerQueue.get (queueId) [hour])

					.addValue (
						"numProcessed",
						numProcessedPerQueue.get (queueId) [hour])

					.addValue (
						"numPreferred",
						numPreferredPerQueue.get (queueId) [hour])

					.addValue (
						"numNotPreferred",
						numNotPreferredPerQueue.get (queueId) [hour]));

			}

		}

		return statsDataSet;

	}

}
