package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.joda.time.Interval;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.html.ObsoleteDateField;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleHelper;
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
	UserConsoleHelper userHelper;

	// state

	Set<QueueItemRec> queueItems;

	UserRec user;

	// implementation

	@Override
	public
	void prepare () {

		ObsoleteDateField dateField =
			ObsoleteDateField.parse (
				requestContext.parameter ("date"));

		int hour =
			Integer.parseInt (
				requestContext.parameter ("hour"));

		int userId =
			Integer.parseInt (
				requestContext.parameter ("userId"));

		user =
			userHelper.find (userId);

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			dateField.date);

		calendar.add (
			Calendar.HOUR,
			hour);

		Date startTime =
			calendar.getTime ();

		calendar.add (
			Calendar.HOUR,
			1);

		Date endTime =
			calendar.getTime ();

		queueItems =
			new TreeSet<QueueItemRec> (
				QueueItemRec.processedTimeComparator);

		queueItems.addAll (
			queueItemHelper.findByProcessedTime (
				user,
				new Interval (
					dateToInstant (startTime),
					dateToInstant (endTime))));

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",

			"%s\n",
			objectManager.tdForObject (
				user,
				null,
				true,
				true),
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

		for (QueueItemRec queueItem
				: queueItems) {

			QueueRec queue =
				queueItem.getQueueSubject () != null
					? queueItem.getQueueSubject ().getQueue ()
					: queueItem.getQueue ();

			Record<?> parent =
				objectManager.getParent (
					queue);

			printFormat (
				"<tr>\n",

				"%s\n",
				objectManager.tdForObject (
					parent,
					null,
					false,
					true),

				"%s\n",
				objectManager.tdForObject (
					queue,
					parent,
					true,
					true),

				"%s\n",
				objectManager.tdForObject (
					queueItem,
					queue,
					true,
					true),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						queueItem.getCreatedTime ())),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						queueItem.getPendingTime ())),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						queueItem.getProcessedTime ())),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
