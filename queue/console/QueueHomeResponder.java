package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.millisToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("queueHomeResponder")
public
class QueueHomeResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	PrivChecker privChecker;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserObjectHelper userHelper;

	// prototype dependencies

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorter;

	// satte

	boolean queueOptionsEnabled = true;

	Instant now;
	UserRec myUser;
	List<QueueItemClaimRec> myClaimedItems;
	List<QueueInfo> queueInfos;

	@Override
	protected
	void prepare () {

		now =
			Instant.now ();

		myUser =
			userHelper.find (
				requestContext.userId ());

		myClaimedItems =
			queueItemClaimHelper.findClaimed (
				myUser);

		// load queue list

		List<QueueInfo> queueInfosTemp =
			queueSubjectSorter.get ()
				.user (myUser)
				.sort ()
				.queues ();

		queueInfos =
			new ArrayList<QueueInfo> ();

		for (QueueInfo queueInfo
				: queueInfosTemp) {

			if (queueInfo.availableItems () == 0)
				continue;

			queueInfos.add (queueInfo);

		}

	}

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		if (! queueOptionsEnabled)
			return super.scriptRefs ();

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/jquery-1.7.1.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/js-yaml-2.1.1.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/wbs.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/queue-home.js"))

			.build ();

	}

	@Override
	protected
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script type=\"text/javascript\">\n",
			"top.show_inbox (true)\n",
			"</script>\n");

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
			"<p class=\"optionSet\">\n",
			"<span class=\"optionSetName\"></span>\n",
			"<span class=\"optionSetOptions\"></span>\n",
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
			"<span class=\"disabledInfo\"></span> ",
			"(<a href=\"javascript:void(0)\" class=\"showHideLink\"></a>)",
			"</p>\n");

		// queue items

		printFormat (
			"<table class=\"list queueItemTable\">\n",

			"<tr>\n",
			"<th>Claim</th>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th>Num</th>\n",
			"<th>Oldest</th>\n",
			"</tr>\n");

		if (queueOptionsEnabled) {

			printFormat (
				"<tr class=\"loadingRow\">\n",
				"<td colspan=\"6\">loading...</td>\n",
				"</tr>\n");
		}

		for (QueueInfo queueInfo : queueInfos) {

			if (queueInfo.availableItems () == 0)
				continue;

			QueueRec queue =
				queueInfo.queue ();

			Record<?> parent =
				objectManager.getParent (
					queue);

			printFormat (
				"<tr",
				" class=\"queueItemRow\"",
				" style=\"%h\"",
				queueOptionsEnabled ? "display:none" : "",
				" data-parent-object-type-code=\"%h\"",
				objectManager.getObjectTypeCode (parent),
				" data-parent-object-code=\"%h\"",
				objectManager.getCode (parent),
				" data-queue-type-code=\"%h\"",
				queue.getQueueType ().getCode (),
				" data-queue-code=\"%h\"",
				queue.getCode (),
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
				">",

				"<input",
				" type=\"submit\"",
				" value=\"claim\"",
				">",

				"</form></td>\n",

				"<td>%h</td>\n",
				objectManager.objectPath (
					objectManager.getParent (
						queue),
					myUser.getSlice (),
					false,
					false),

				"<td>%h</td>\n",
				queue.getCode (),

				"<td>%h</td>\n",
				queueInfo.availableItems (),

				"<td class=\"queueItemOldest\">%h</td>\n",
				requestContext.prettyDateDiff (
					millisToInstant (queueInfo.oldestAvailable ()),
					now),

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

		for (QueueItemClaimRec queueItemClaim
				: myClaimedItems) {

			QueueItemRec queueItem =
				queueItemClaim.getQueueItem ();

			QueueSubjectRec queueSubject =
				queueItem.getQueueSubject ();

			QueueRec queue =
				queueSubject.getQueue ();

			String url =
				requestContext.resolveApplicationUrl (
					stringFormat (
						"/queues",
						"/queue.item",
						"?id=%s",
						queueItem.getId ()));

			printFormat (
				"%s",
				Html.magicTr (
					url,
					false,
					null));

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
					objectManager.getParent (queue),
					myUser.getSlice (),
					false,
					false));

			printFormat (
				"<td>%h</td>\n",
				queue.getCode (),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (queueItem.getCreatedTime ())),

				"<td>%h</td>\n",
				queueItem.getSource (),

				"<td>%h</td>\n",
				queueItem.getDetails (),

				"</tr>\n");

			if (maxItems-- == 0)
				break;

		}

		printFormat (
			"</table>\n");

	}

	@Override
	protected
	void goBodyStuff () {

		printFormat (
			"<p class=\"links\">\n",
			"<a href=\"%h\">Refresh</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"<a href=\"#\" onclick=\"top.show_inbox (false)\">Close</a>\n",
			"</p>\n");

		requestContext.flushNotices (out);

		goQueues ();

		goMyItems ();

	}

}
