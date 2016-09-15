package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;
import java.util.TreeSet;

import org.joda.time.Interval;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.platform.queue.model.QueueItemProcessedTimeComparator;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("queueSupervisorItemsPart")
public
class QueueSupervisorItemsPart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
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

		renderParameterTable ();

		renderContentTable ();
	}

	private
	void renderParameterTable () {

		// open table

		htmlTableOpenDetails ();

		// write user table row

		htmlTableDetailsRowWriteRaw (
			"User",
			() -> 
				objectManager.writeTdForObjectMiniLink (
					user));

		// close table

		htmlTableClose ();

	}

	private
	void renderContentTable () {

		// open table

		htmlTableOpenList ();

		// write table header

		htmlTableHeaderRowWrite (
			"Object",
			"Queue",
			"Item",
			"Created",
			"Pending",
			"Processed");

		// write table contents

		for (
			QueueItemRec queueItem
				: queueItems
		) {

			QueueRec queue =
				ifNotNullThenElse (
					queueItem.getQueueSubject (),
					() -> queueItem.getQueueSubject ().getQueue (),
					() -> queueItem.getQueue ());

			Record <?> parent =
				objectManager.getParent (
					queue);

			// open table row

			htmlTableRowOpen ();

			objectManager.writeTdForObjectLink (
				parent);

			objectManager.writeTdForObjectMiniLink (
				queue,
				parent);

			objectManager.writeTdForObjectMiniLink (
				queueItem,
				queue);

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getCreatedTime ()));

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getPendingTime ()));

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getProcessedTime ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
