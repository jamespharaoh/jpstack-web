package wbs.platform.queue.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;
import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.utils.web.HtmlUtils;

@PrototypeComponent ("queueHomeResponder")
public
class QueueHomeResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	DummyQueueCache dummyQueueCache;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	boolean queueOptionsEnabled = true;

	List <QueueItemClaimRec> myClaimedItems;
	List <QueueInfo> queueInfos;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		if (queueOptionsEnabled) {

			return ImmutableSet.<ScriptRef>builder ()

				.addAll (
					super.scriptRefs ())

				.add (
					JqueryScriptRef.instance)

				.add (
					MagicTableScriptRef.instance)

				.add (
					ConsoleApplicationScriptRef.javascript (
						"/js/js-yaml-2.1.1.js"))

				.add (
					ConsoleApplicationScriptRef.javascript (
						"/js/wbs.js"))

				.add (
					ConsoleApplicationScriptRef.javascript (
						"/js/queue-home.js"))

				.build ();

		} else {

			return ImmutableSet.<ScriptRef>builder ()

				.addAll (
					super.scriptRefs ())

				.add (
					JqueryScriptRef.instance)

				.add (
					MagicTableScriptRef.instance)

				.build ();

		}

	}

	// implementation

	@Override
	protected
	void prepare () {

		myClaimedItems =
			queueItemClaimHelper.findClaimed (
				userConsoleLogic.userRequired ());

		// load queue list

		List<QueueInfo> queueInfosTemp =
			queueSubjectSorterProvider.get ()

			.queueCache (
				dummyQueueCache)

			.loggedInUser (
				userConsoleLogic.userRequired ())

			.effectiveUser (
				userConsoleLogic.userRequired ())

			.sort ()

			.availableQueues ();

		queueInfos =
			new ArrayList<QueueInfo> ();

		for (
			QueueInfo queueInfo
				: queueInfosTemp
		) {

			if (queueInfo.availableItems () == 0)
				continue;

			queueInfos.add (queueInfo);

		}

	}

	@Override
	protected
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<script type=\"text/javascript\">\n",
			"top.show_inbox (true)\n",
			"</script>\n");

		// collect list of queues for stylesheet

		Set<QueueRec> queues =
			new HashSet<QueueRec> ();

		// unclaimed items

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			if (queueInfo.availableItems () == 0)
				continue;

			queues.add (
				queueInfo.queue ());

		}

		// claimed items

		for (
			QueueItemClaimRec queueItemClaim
				: myClaimedItems
		) {

			QueueItemRec queueItem =
				queueItemClaim.getQueueItem ();

			QueueSubjectRec queueSubject =
				queueItem.getQueueSubject ();

			QueueRec queue =
				queueSubject.getQueue ();

			queues.add (
				queue);

		}

		// output styles

		if (
			collectionIsNotEmpty (
				queues)
		) {

			printFormat (
				"<style type=\"text/css\">\n");

			for (
				QueueRec queue
					: queues.stream ()
						.sorted ()
						.collect (Collectors.toList ())
			) {

				if (

					isNull (
						queue.getBackgroundColour ())

					&& isNull (
						queue.getForegroundColour ())

				) {
					continue;
				}

				printFormat (
					"\ttable.list tr.queue-%h td {\n",
					queue.getId ());

				if (
					isNotNull (
						queue.getBackgroundColour ())
				) {

					printFormat (
						"\t\tbackground-color: %s;\n",
						queue.getBackgroundColour ());

				}

				if (
					isNotNull (
						queue.getForegroundColour ())
				) {

					printFormat (
						"\t\tcolor: %s;\n",
						queue.getForegroundColour ());

				}

				printFormat (
					"\t}\n");

			}

			printFormat (
				"</style>\n");

		}

	}

	protected
	void goQueues () {

		printFormat (
			"<h2>Queues</h2>\n");

		if (queueInfos.isEmpty ()) {

			printFormat (
				"<p>Nothing to claim</p>\n");

			return;

		}

		// templates

		printFormat (
			"<div style=\"display:none\">\n");

		printFormat (
			"<p",
			" class=\"optionSet\"",
			">\n");

		printFormat (
			"<span",
			" class=\"optionSetName\"",
			"></span>\n");

		printFormat (
			"<span",
			" class=\"optionSetOptions\"",
			"></span>\n");

		printFormat (
			"</p>\n");

		printFormat (
			"<span class=\"option\">\n",
			"<input type=\"checkbox\" class=\"optionCheckbox\">\n",
			"<label class=\"optionLabel\"></label>\n",
			"</span>\n");

		printFormat (
			"</div>\n");

		// option sets

		printFormat (
			"<div class=\"optionSets\"></div>\n");

		printFormat (
			"<p>",

			"<span",
			" class=\"disabledInfo\"",
			"></span> ",

			"(<a",
			" href=\"javascript:void(0)\"",
			" class=\"showHideLink\"",
			"></a>)",

			"</p>\n");

		// queue items

		printFormat (
			"<table",
			" class=\"list queueItemTable\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Claim</th>\n",
			"<th>Type</th>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th>Num</th>\n",
			"<th>Pri</th>\n",
			"<th>Oldest</th>\n",
			"</tr>\n");

		if (queueOptionsEnabled) {

			printFormat (
				"<tr class=\"loadingRow\">\n",
				"<td colspan=\"6\">loading...</td>\n",
				"</tr>\n");
		}

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			if (queueInfo.availableItems () == 0)
				continue;

			QueueRec queue =
				queueInfo.queue ();

			Record<?> parent =
				objectManager.getParent (
					queue);

			Optional<SliceRec> slice =
				objectManager.getAncestor (
					SliceRec.class,
					queue);

			String parentTypeCode =
				objectManager.getObjectTypeCode (
					parent);

			String parentCode =
				objectManager.getCode (
					parent);

			printFormat (
				"<tr",

				" class=\"%h\"",
				joinWithSpace (

					"queueItemRow",

					stringFormat (
						"queue-%h",
						queue.getId ())),

				" style=\"%h\"",
				queueOptionsEnabled
					? "display:none"
					: "",

				" data-parent-object-type-code=\"%h\"",
				parentTypeCode,

				" data-parent-object-code=\"%h\"",
				parentCode,

				" data-queue-type-code=\"%h\"",
				queue.getQueueType ().getCode (),

				" data-queue-code=\"%h\"",
				queue.getCode (),

				" data-slice-code=\"%h\"",
				slice != null
					? slice.get ().getCode ()
					: "",

				" data-oldest-timestamp=\"%h\"",
				queueInfo.oldestAvailable (),

				">\n");

			printFormat (
				"<td><form",
				" action=\"%h\"",
				requestContext.resolveApplicationUrl (
					"/queues/queue.claim"),
				" method=\"post\"",
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"queue_id\"",
				" value=\"%h\"",
				queue.getId (),
				">");

			printFormat (
				"<input",
				" type=\"submit\"",
				" value=\"claim\"",
				">");

			printFormat (
				"</form></td>\n");

			printFormat (
				"<td>%h</td>\n",
				parentTypeCode);

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPathMini (
					parent,
					userConsoleLogic.sliceRequired ()));

			printFormat (
				"<td>%h</td>\n",
				queue.getCode ());

			printFormat (
				"<td>%h</td>\n",
				queueInfo.availableItems ());

			if (
				privChecker.canRecursive (
					objectManager.getParent (
						queueInfo.queue ()),
					"supervisor")
			) {

				printFormat (
					"<td>%h</td>\n",
					queueInfo.highestPriorityAvailable ());

			} else {

				printFormat (
					"<td></td>\n");

			}

			printFormat (
				"<td",
				" class=\"queueItemOldest\"",
				">%s</td>\n",
				HtmlUtils.htmlEncodeNonBreakingWhitespace (
					userConsoleLogic.prettyDuration (
						queueInfo.oldestAvailable (),
						transaction.now ())));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	protected
	void goMyItems () {

		if (myClaimedItems.size () == 0)
			return;

		printFormat (
			"<h2>My items</h2>\n");

		printFormat (
			"<table",
			" class=\"list\"",
			" border=\"0\"",
			" cellspacing=\"1\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Unclaim</th>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Source</th>\n",
			"<th>Details</th>\n",
			"</tr>\n");

		int maxItems = 100;

		for (
			QueueItemClaimRec queueItemClaim
				: myClaimedItems
		) {

			QueueItemRec queueItem =
				queueItemClaim.getQueueItem ();

			QueueSubjectRec queueSubject =
				queueItem.getQueueSubject ();

			QueueRec queue =
				queueSubject.getQueue ();

			printFormat (
				"<tr",

				" class=\"%h\"",
				joinWithSpace (
					"magic-table-row",
					stringFormat (
						"queue-%h",
						queue.getId ())),

				" data-target-href=\"%h\"",
				requestContext.resolveApplicationUrl (
					stringFormat (
						"/queues",
						"/queue.item",
						"?id=%s",
						queueItem.getId ())),

				">\n");

			printFormat (
				"<td><form",

				" action=\"%h\"",
				requestContext.resolveApplicationUrl (
					"/queues/queue.unclaim"),

				" method=\"post\"",
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"queueItemId\"",
				" value=\"%h\"",
				queueItem.getId (),
				">\n");

			printFormat (
				"<input",
				" type=\"submit\"",
				" value=\"unclaim\"",
				">\n");

			printFormat (
				"</form></td>\n");

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPath (
					objectManager.getParent (
						queue),
					userConsoleLogic.sliceRequired ()));

			printFormat (
				"<td>%h</td>\n",
				queue.getCode ());

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getCreatedTime ()));

			printFormat (
				"<td>%h</td>\n",
				queueItem.getSource ());

			printFormat (
				"<td>%h</td>\n",
				queueItem.getDetails ());

			printFormat (
				"</tr>\n");

			if (maxItems-- == 0)
				break;

		}

		printFormat (
			"</table>\n");

	}

	@Override
	protected
	void renderHtmlBodyContents () {

		printFormat (
			"<p class=\"links\">\n",
			"<a href=\"%h\">Refresh</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"<a href=\"#\" onclick=\"top.show_inbox (false)\">Close</a>\n",
			"</p>\n");

		requestContext.flushNotices (
			formatWriter);

		goQueues ();

		goMyItems ();

	}

}
