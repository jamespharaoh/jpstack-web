package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;
import java.util.TreeSet;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			Interval interval =
				timeFormatter.isoStringToInterval (
					requestContext.parameterRequired (
						"interval"));

			user =
				userHelper.findRequired (
					transaction,
					requestContext.parameterIntegerRequired (
						"userId"));

			queueItems =
				new TreeSet<> (
					QueueItemProcessedTimeComparator.instance);

			queueItems.addAll (
				queueItemHelper.findByProcessedTime (
					transaction,
					user,
					interval));

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			renderParameterTable (
				transaction);

			renderContentTable (
				transaction);

		}

	}

	private
	void renderParameterTable (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderParameterTable");

		) {

			// open table

			htmlTableOpenDetails ();

			// write user table row

			htmlTableDetailsRowWriteRaw (
				"User",
				() ->
					objectManager.writeTdForObjectMiniLink (
						transaction,
						user));

			// close table

			htmlTableClose ();

		}

	}

	private
	void renderContentTable (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderContentTable");

		) {

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
					objectManager.getParentRequired (
						transaction,
						queue);

				// open table row

				htmlTableRowOpen ();

				objectManager.writeTdForObjectLink (
					transaction,
					parent);

				objectManager.writeTdForObjectMiniLink (
					transaction,
					queue,
					parent);

				objectManager.writeTdForObjectMiniLink (
					transaction,
					queueItem,
					queue);

				htmlTableCellWrite (
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						queueItem.getCreatedTime ()));

				htmlTableCellWrite (
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						queueItem.getPendingTime ()));

				htmlTableCellWrite (
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						queueItem.getProcessedTime ()));

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

}
