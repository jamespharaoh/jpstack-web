package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.ifThenElseEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("queueListActivePart")
public
class QueueListActivePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	DummyQueueCache dummyQueueCache;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	List <QueueInfo> queueInfos;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		List <QueueInfo> queueInfosTemp =
			queueSubjectSorterProvider.get ()

			.queueCache (
				dummyQueueCache)

			.loggedInUser (
				userConsoleLogic.userRequired ())

			.sort ()

			.availableQueues ();

		queueInfos =
			new ArrayList <> ();

		for (
			QueueInfo queueInfo
				: queueInfosTemp
		) {

			if (
				! objectManager.canView (
					queueInfo.queue ())
			) {
				continue;
			}

			queueInfos.add (
				queueInfo);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		ConsoleContextType queueContextType =
			consoleManager.contextType (
				"queue:object",
				true);

		ConsoleContext queueContext =
			consoleManager.relatedContextRequired (
				requestContext.consoleContext (),
				queueContextType);

		htmlTableHeaderRowWrite (
			"Object",
			"Queue",
			"Available",
			null,
			"Claimed",
			null,
			"Preferred",
			null,
			"Waiting",
			null,
			"Total",
			null);

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			QueueRec queue =
				queueInfo.queue ();

			// table row open

			htmlTableRowOpen (
				htmlClassAttribute (
					"magic-table-row"),
				htmlDataAttribute (
					"target-href",
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							queueContext.pathPrefix (),
							"/%u",
							queue.getId ()))));

			// details

			htmlTableCellWrite (
				objectManager.objectPath (
					objectManager.getParent (
						queue)));

			htmlTableCellWrite (
				queue.getCode ());

			// available

			htmlTableCellWrite (
				integerToDecimalString (
					queueInfo.availableItems ()));

			htmlTableCellWrite (
				ifThenElseEmDash (
					moreThanZero (
						queueInfo.availableItems ()),
					() -> timeFormatter.prettyDuration (
						queueInfo.oldestAvailable (),
						transaction.now ())));

			// claimed

			htmlTableCellWrite (
				integerToDecimalString (
					queueInfo.claimedItems ()));

			htmlTableCellWrite (
				ifThenElseEmDash (
					moreThanZero (
						queueInfo.claimedItems ()),
					() -> timeFormatter.prettyDuration (
						queueInfo.oldestClaimed (),
						transaction.now ())));

			// preferred

			htmlTableCellWrite (
				integerToDecimalString (
					queueInfo.totalUnavailableItems ()));

			htmlTableCellWrite (
				ifThenElseEmDash (
					moreThanZero (
						queueInfo.totalUnavailableItems ()),
					() -> timeFormatter.prettyDuration (
						queueInfo.oldestUnavailable (),
						transaction.now ())));

			// waiting

			htmlTableCellWrite (
				integerToDecimalString (
					queueInfo.waitingItems ()));

			htmlTableCellWrite (
				ifThenElseEmDash (
					moreThanZero (
						queueInfo.waitingItems ()),
					() -> timeFormatter.prettyDuration (
						queueInfo.oldestWaiting (),
						transaction.now ())));

			// total

			htmlTableCellWrite (
				integerToDecimalString (
					queueInfo.totalItems ()));

			htmlTableCellWrite (
				ifThenElseEmDash (
					moreThanZero (
						queueInfo.totalItems ()),
					() -> timeFormatter.prettyDuration (
						queueInfo.oldest (),
						transaction.now ())));

			// table row close

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

	}

}
