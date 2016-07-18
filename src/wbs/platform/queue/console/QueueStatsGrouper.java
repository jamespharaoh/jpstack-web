package wbs.platform.queue.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsGrouper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.queue.model.QueueRec;

@SingletonComponent ("queueStatsGrouper")
public
class QueueStatsGrouper
	implements StatsGrouper {

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	QueueConsoleHelper queueHelper;

	@Override
	public
	Set<Object> getGroups (
			StatsDataSet dataSet) {

		return new HashSet<Object> (
			dataSet.indexValues ().get ("queueId"));

	}

	@Override
	public
	String tdForGroup (
			Object group) {

		QueueRec queue =
			queueHelper.findRequired (
				(Integer)
				group);

		return consoleObjectManager.tdForObjectMiniLink (
			queue);

	}

	@Override
	public
	List<Object> sortGroups (
			Set<Object> groups) {

		List<QueueRec> queues =
			new ArrayList<QueueRec> (
				groups.size ());

		for (Object group : groups) {

			Integer queueId =
				(Integer) group;

			queues.add (
				queueHelper.findRequired (
					queueId));

		}

		Collections.sort (
			queues);

		ArrayList<Object> queueIds =
			new ArrayList<Object> (
				queues.size ());

		for (QueueRec queue : queues)
			queueIds.add (queue.getId ());

		return queueIds;

	}

}
