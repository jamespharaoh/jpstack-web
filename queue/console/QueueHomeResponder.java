package wbs.platform.queue.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlDivClose;
import static wbs.utils.web.HtmlBlockUtils.htmlDivOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlDivWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlSpanClose;
import static wbs.utils.web.HtmlBlockUtils.htmlSpanOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlSpanWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockWrite;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlEncodeNonBreakingWhitespace;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;
import static wbs.utils.web.HtmlUtils.htmlLinkWriteInline;

import java.util.ArrayList;
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

	Set <QueueRec> queues;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		if (queueOptionsEnabled) {

			return ImmutableSet.<ScriptRef> builder ()

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

			return ImmutableSet.<ScriptRef> builder ()

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

		List <QueueInfo> queueInfosTemp =
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
			new ArrayList<> ();

		for (
			QueueInfo queueInfo
				: queueInfosTemp
		) {

			if (queueInfo.availableItems () == 0)
				continue;

			queueInfos.add (queueInfo);

		}

		// unclaimed items

		queues =
			ImmutableSet.<QueueRec> builder ()

			.addAll (
				queueInfos.stream ()

				.filter (
					queueInfo ->
						moreThanZero (
							queueInfo.availableItems ()))

				.map (
					queueInfo ->
						queueInfo.queue ())

				.iterator ()

			)

			.addAll (
				myClaimedItems.stream ()

				.map (
					QueueItemClaimRec::getQueueItem)

				.map (
					QueueItemRec::getQueueSubject)

				.map (
					QueueSubjectRec::getQueue)

				.iterator ()

			)

			.build ();

	}

	@Override
	protected
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		htmlScriptBlockWrite (
			"top.show_inbox (true)");

		renderStyles ();

	}

	private
	void renderStyles () {

		if (
			collectionIsEmpty (
				queues)
		) {
			return;
		}

		htmlStyleBlockOpen ();

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

			htmlStyleRuleOpen (
				stringFormat (
					"table.list tr.queue-%h td",
					queue.getId ()));

			if (
				isNotNull (
					queue.getBackgroundColour ())
			) {

				htmlStyleRuleEntryWrite (
					"background-color",
					queue.getBackgroundColour ());

			}

			if (
				isNotNull (
					queue.getForegroundColour ())
			) {

				htmlStyleRuleEntryWrite (
					"color",
					queue.getForegroundColour ());

			}

			htmlStyleRuleClose ();

		}

		htmlStyleBlockClose ();

	}

	protected
	void goQueues () {

		htmlHeadingTwoWrite (
			"Queues");

		// nothing to claim

		if (queueInfos.isEmpty ()) {

			htmlParagraphWrite (
				"Nothing to claim");

			return;

		}

		// render queue stuff

		renderOptions ();
		renderQueueItems ();

	}

	private
	void renderOptions () {

		// templates start

		htmlDivOpen (
			htmlAttribute (
				"style",
				"display: none"));

		// option set template

		htmlParagraphOpen (
			htmlClassAttribute (
				"optionSet"));

		htmlSpanWrite (
			"",
			htmlClassAttribute (
				"optionSetName"));

		htmlSpanWrite (
			"",
			htmlClassAttribute (
				"optionSetOptions"));

		htmlParagraphClose ();

		// option template

		htmlSpanOpen (
			htmlClassAttribute (
				"option"));

		formatWriter.writeLineFormat (
			"<input",
			" type=\"checkbox\"",
			" class=\"optionCheckbox\"",
			">");

		formatWriter.writeLineFormat (
			"<label",
			" class=\"optionLabel\"",
			"></label>");

		htmlSpanClose ();

		htmlDivClose ();

		// option sets

		htmlDivWrite (
			"",
			htmlClassAttribute (
				"optionSets"));

		// enable/disable link

		htmlParagraphOpen ();

		htmlSpanWrite (
			"",
			htmlClassAttribute (
				"disabledInfo"));

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"(");

		htmlLinkWriteInline (
			"javascript:void (0)",
			"",
			htmlClassAttribute (
				"showHideLink"));

		formatWriter.writeFormat (
			")");

		formatWriter.writeNewline ();

		htmlParagraphClose ();

	}

	private
	void renderQueueItems () {

		// table open

		htmlTableOpen (
			htmlClassAttribute (
				"list",
				"queueItemTable"));

		htmlTableHeaderRowWrite (
			"Claim",
			"Type",
			"Object",
			"Queue",
			"Num",
			"Pri",
			"Oldest");

		if (queueOptionsEnabled) {

			htmlTableRowOpen (
				htmlClassAttribute (
					"loadingRow"));

			htmlTableCellWrite (
				"loading...",
				htmlColumnSpanAttribute (6l));

			htmlTableRowClose ();

		}

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			if (queueInfo.availableItems () == 0)
				continue;

			QueueRec queue =
				queueInfo.queue ();

			Record <?> parent =
				objectManager.getParentOrNull (
					queue);

			Optional <SliceRec> slice =
				objectManager.getAncestor (
					SliceRec.class,
					queue);

			String parentTypeCode =
				objectManager.getObjectTypeCode (
					parent);

			String parentCode =
				objectManager.getCode (
					parent);

			// table row open

			htmlTableRowOpen (

				htmlClassAttribute (
					joinWithSpace (
						"queueItemRow",
						stringFormat (
							"queue-%h",
							queue.getId ()))),

				htmlStyleAttribute (
					presentInstances (
						optionalIf (
							queueOptionsEnabled,
							() -> htmlStyleRuleEntry (
								"display",
								"none")))),

				htmlDataAttribute (
					"parent-object-type-code",
					parentTypeCode),

				htmlDataAttribute (
					"parent-object-code",
					parentCode),

				htmlDataAttribute (
					"queue-type-code",
					queue.getQueueType ().getCode ()),

				htmlDataAttribute (
					"queue-code",
					queue.getCode ()),

				htmlDataAttribute (
					"slice-code",
					ifNotNullThenElse (
						slice,
						() -> slice.get ().getCode (),
						() -> "")),

				htmlDataAttribute (
					"oldest-timestamp",
					queueInfo.oldestAvailable ().toString ())

			);

			// claim cell

			htmlTableCellOpen ();

			htmlFormOpenPostAction (
				requestContext.resolveApplicationUrl (
					"/queues/queue.claim"));

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"queue_id\"",
				" value=\"%h\"",
				queue.getId (),
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"claim\"",
				">");

			htmlFormClose ();

			htmlTableCellClose ();

			// type code

			htmlTableCellWrite (
				parentTypeCode);

			// object path

			htmlTableCellWrite (
				objectManager.objectPathMini (
					parent,
					userConsoleLogic.sliceRequired ()));

			// queue code

			htmlTableCellWrite (
				queue.getCode ());

			// available items

			htmlTableCellWrite (
				integerToDecimalString (
					queueInfo.availableItems ()));

			// priority

			if (
				privChecker.canRecursive (
					objectManager.getParentOrNull (
						queueInfo.queue ()),
					"supervisor")
			) {

				htmlTableCellWrite (
					integerToDecimalString (
						queueInfo.highestPriorityAvailable ()));

			} else {

				htmlTableCellWrite (
					"");

			}

			// oldest

			htmlTableCellWriteHtml (
				htmlEncodeNonBreakingWhitespace (
					userConsoleLogic.prettyDuration (
						queueInfo.oldestAvailable (),
						transaction.now ())),
				htmlClassAttribute (
					"queueItemOldest"));

			// table row close

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

	}

	protected
	void goMyItems () {

		if (
			collectionIsEmpty (
				myClaimedItems)
		) {
			return;
		}

		htmlHeadingTwoWrite (
			"My items");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Unclaim",
			"Object",
			"Queue",
			"Timestamp",
			"Source",
			"Details");

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

			htmlTableRowOpen (

				htmlClassAttribute (
					"magic-table-row",
					stringFormat (
						"queue-%h",
						queue.getId ())),

				htmlDataAttribute (
					"target-href",
					requestContext.resolveApplicationUrl (
						stringFormat (
							"/queues",
							"/queue.item",
							"?id=%s",
							queueItem.getId ())))

			);

			htmlTableCellOpen ();

			htmlFormOpenPostAction (
				requestContext.resolveApplicationUrl (
					"/queues/queue.unclaim"));

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"queueItemId\"",
				" value=\"%h\"",
				queueItem.getId (),
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"unclaim\"",
				">");

			htmlFormClose ();

			htmlTableCellClose ();

			htmlTableCellWrite (
				objectManager.objectPath (
					objectManager.getParentOrNull (
						queue),
					userConsoleLogic.sliceRequired ()));

			htmlTableCellWrite (
				queue.getCode ());

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					queueItem.getCreatedTime ()));

			htmlTableCellWrite (
				queueItem.getSource ());

			htmlTableCellWrite (
				queueItem.getDetails ());

			htmlTableRowClose ();

			if (maxItems -- == 0)
				break;

		}

		htmlTableClose ();

	}

	@Override
	protected
	void renderHtmlBodyContents () {

		renderLinks ();

		requestContext.flushNotices (
			formatWriter);

		goQueues ();

		goMyItems ();

	}

	private
	void renderLinks () {

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"Refresh");

		htmlLinkWrite (
			"#",
			"Close",

			htmlAttribute (
				"onclick",
				"top.show_inbox (false);")

		);

		htmlParagraphClose ();

	}

}
