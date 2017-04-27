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
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			renderParameterTable (
				taskLogger);

			renderContentTable (
				taskLogger);

		}

	}

	private
	void renderParameterTable (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderParameterTable");

		) {

			// open table

			htmlTableOpenDetails ();

			// write user table row

			htmlTableDetailsRowWriteRaw (
				"User",
				() ->
					objectManager.writeTdForObjectMiniLink (
						taskLogger,
						user));

			// close table

			htmlTableClose ();

		}

	}

	private
	void renderContentTable (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
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
						queue);

				// open table row

				htmlTableRowOpen ();

				objectManager.writeTdForObjectLink (
					taskLogger,
					parent);

				objectManager.writeTdForObjectMiniLink (
					taskLogger,
					queue,
					parent);

				objectManager.writeTdForObjectMiniLink (
					taskLogger,
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

}
