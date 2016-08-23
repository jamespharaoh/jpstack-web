package wbs.platform.queue.console;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.joda.time.Interval;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.queue.model.QueueItemProcessedTimeComparator;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueSupervisorItemsPart")
public
class QueueSupervisorItemsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	QueueItemConsoleHelper queueItemHelper;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	// state

	Set<QueueItemRec> queueItems;

	UserRec user;

	// implementation

	@Override
	public
	void prepare () {

		Interval interval =
			timeFormatter.isoStringToInterval (
				requestContext.parameterRequired (
					"interval"));

		user =
			userHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"userId"));

		queueItems =
			new TreeSet<QueueItemRec> (
				QueueItemProcessedTimeComparator.instance);

		queueItems.addAll (
			queueItemHelper.findByProcessedTime (
				user,
				interval));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				user),
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th>Item</th>\n",
			"<th>Created</th>\n",
			"<th>Pending</th>\n",
			"<th>Processed</th>\n",
			"</tr>\n");

		for (
			QueueItemRec queueItem
				: queueItems
		) {

			QueueRec queue =
				queueItem.getQueueSubject () != null
					? queueItem.getQueueSubject ().getQueue ()
					: queueItem.getQueue ();

			Record<?> parent =
				objectManager.getParent (
					queue);

			printFormat (
				"<tr>\n");

			printFormat (
				"%s\n",
				objectManager.tdForObjectLink (
					parent));

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					queue,
					parent));

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					queueItem,
					queue));

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getCreatedTime ()));

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getPendingTime ()));

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getProcessedTime ()));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
