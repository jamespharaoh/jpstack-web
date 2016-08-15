package wbs.platform.queue.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
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
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("queueListActivePart")
public
class QueueListActivePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	DummyQueueCache dummyQueueCache;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	List<QueueInfo> queueInfos;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

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

		List<QueueInfo> queueInfosTemp =
			queueSubjectSorterProvider.get ()

			.queueCache (
				dummyQueueCache)

			.loggedInUser (
				userConsoleLogic.userRequired ())

			.sort ()

			.availableQueues ();

		queueInfos =
			new ArrayList<QueueInfo> ();

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

		printFormat (
			"<table class=\"list\">\n");

		ConsoleContextType queueContextType =
			consoleManager.contextType (
				"queue:object",
				true);

		ConsoleContext queueContext =
			consoleManager.relatedContextRequired (
				requestContext.consoleContext (),
				queueContextType);

		printFormat (
			"<tr>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th colspan=\"2\">Available</th>\n",
			"<th colspan=\"2\">Claimed</th>\n",
			"<th colspan=\"2\">Preferred</th>\n",
			"<th colspan=\"2\">Waiting</th>\n",
			"<th colspan=\"2\">Total</th>\n",
			"</tr>\n");

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			QueueRec queue =
				queueInfo.queue ();

			printFormat (
				"<tr",
				" class=\"magic-table-row\"",

				" data-target-href=\"%h\"",
				requestContext.resolveContextUrl (
					stringFormat (
						"%s",
						queueContext.pathPrefix (),
						"/%u",
						queue.getId ())),

				">\n");

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPath (
					objectManager.getParent (
						queue)));

			printFormat (
				"<td>%h</td>\n",
				queue.getCode ());

			// available

			printFormat (
				"<td>%h</td>\n",
				queueInfo.availableItems ());

			printFormat (
				"<td>%h</td>\n",
				queueInfo.availableItems () > 0
					? timeFormatter.prettyDuration (
						queueInfo.oldestAvailable (),
						transaction.now ())
					: "-");

			// claimed

			printFormat (
				"<td>%h</td>\n",
				queueInfo.claimedItems ());

			printFormat (
				"<td>%h</td>\n",
				queueInfo.claimedItems () > 0
					? timeFormatter.prettyDuration (
						queueInfo.oldestClaimed (),
						transaction.now ())
					: "-");

			// preferred

			printFormat (
				"<td>%h</td>\n",
				queueInfo.totalUnavailableItems ());

			printFormat (
				"<td>%h</td>\n",
				queueInfo.totalUnavailableItems () > 0
					? timeFormatter.prettyDuration (
						queueInfo.oldestUnavailable (),
						transaction.now ())
					: "-");

			// waiting

			printFormat (
				"<td>%h</td>\n",
				queueInfo.waitingItems ());

			printFormat (
				"<td>%h</td>\n",
				queueInfo.waitingItems () > 0
					? timeFormatter.prettyDuration (
						queueInfo.oldestWaiting (),
						transaction.now ())
					: "-");

			// total

			printFormat (
				"<td>%h</td>\n",
				queueInfo.totalItems ());

			printFormat (
				"<td>%h</td>\n",
				queueInfo.totalItems () > 0
					? timeFormatter.prettyDuration (
						queueInfo.oldest (),
						transaction.now ())
					: "-");

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
