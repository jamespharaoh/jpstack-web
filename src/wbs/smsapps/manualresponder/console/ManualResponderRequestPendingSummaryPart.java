package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlDivClose;
import static wbs.utils.web.HtmlBlockUtils.htmlDivOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenLayout;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.utils.web.HtmlUtils.htmlLinkWriteHtml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.builder.CompareToBuilder;

import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;
import wbs.utils.time.TextualInterval;
import wbs.utils.web.HtmlUtils;

@Log4j
@PrototypeComponent ("manualResponderRequestPendingSummaryPart")
public
class ManualResponderRequestPendingSummaryPart
	extends AbstractPagePart {

	final
	long maxResults = 1000l;

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@SingletonDependency
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@SingletonDependency
	@Named
	ConsoleModule manualResponderRequestPendingConsoleModule;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	FormFieldSet customerDetailsFields;

	ManualResponderRequestRec manualResponderRequest;
	ManualResponderNumberRec manualResponderNumber;
	ManualResponderRec manualResponder;

	SmsCustomerRec smsCustomer;
	SmsCustomerSessionRec smsCustomerSession;

	MessageRec message;
	NumberRec number;
	NetworkRec network;

	List<ManualResponderRequestRec> oldRequests;
	List<RouteBillInfo> routeBillInfos;

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
				JqueryEditableScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/manual-responder.js"))

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		customerDetailsFields =
			manualResponderRequestPendingConsoleModule.formFieldSets ().get (
				"customer-details");

		manualResponderRequest =
			manualResponderRequestHelper.findRequired (
				requestContext.stuffInteger (
					"manualResponderRequestId"));

		manualResponderNumber =
			manualResponderRequest.getManualResponderNumber ();

		manualResponder =
			manualResponderNumber.getManualResponder ();

		message =
			manualResponderRequest.getMessage ();

		number =
			manualResponderRequest.getNumber ();

		network =
			number.getNetwork ();

		smsCustomer =
			manualResponderNumber != null
				? manualResponderNumber.getSmsCustomer ()
				: null;

		smsCustomerSession =
			smsCustomer != null
			&& smsCustomer.getNumSessions () > 0

			? smsCustomerSessionHelper.findByIndexRequired (
				smsCustomer,
				smsCustomer.getNumSessions () - 1)

			: null;

		// get routes

		Set <RouteRec> routes =
			new HashSet<> ();

		for (
			ManualResponderTemplateRec manualResponderTemplate
				: manualResponder.getTemplates ()
		) {

			RouteRec route =
				routerLogic.resolveRouter (
					manualResponderTemplate.getRouter ());

			if (route == null)
				continue;

			if (route.getOutCharge () == 0)
				continue;

			routes.add (
				route);

		}

		// get bill counts per route

		Instant startOfToday =
			LocalDate.now ()
				.toDateTimeAtStartOfDay ()
				.toInstant ();

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				manualResponder,
				"default");

		routeBillInfos =
			new ArrayList <RouteBillInfo> ();

		for (
			RouteRec route
				: routes
		) {

			long total = 0l;
			long thisService = 0l;

			MessageSearch messageSearch =
				new MessageSearch ()

				.numberId (
					number.getId ())

				.routeId (
					route.getId ())

				.createdTime (
					TextualInterval.after (
						userConsoleLogic.timezone (),
						startOfToday))

				.direction (
					MessageDirection.out);

			List <MessageRec> messages =
				messageHelper.search (
					messageSearch);

			for (
				MessageRec message
					: messages
			) {

				if (
					enumInSafe (
						message.getStatus (),
						MessageStatus.undelivered,
						MessageStatus.cancelled,
						MessageStatus.reportTimedOut)
				) {

					continue;

				}

				if (
					enumNotInSafe (
						message.getStatus (),
						MessageStatus.pending,
						MessageStatus.sent,
						MessageStatus.submitted,
						MessageStatus.delivered)
				) {

					log.error (
						stringFormat (
							"Counting message in unknown state %s",
							message.getStatus ()));

				}

				total +=
					route.getOutCharge ();

				if (
					referenceEqualWithClass (
						ServiceRec.class,
						message.getService (),
						defaultService)
				) {

					thisService +=
						route.getOutCharge ();

				}

			}

			routeBillInfos.add (
				new RouteBillInfo ()

				.route (
					route)

				.total (
					total)

				.thisService (
					thisService)

			);

		}

		Collections.sort (
			routeBillInfos);

		// get request history

		oldRequests =
			manualResponderRequestHelper.findRecentLimit (
				manualResponder,
				manualResponderRequest.getNumber (),
				maxResults + 1);

		Collections.sort (
			oldRequests);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (manualResponderRequest == null) {

			formatWriter.writeLineFormat (
				"<p>Not found</p>");

			return;

		}

		htmlDivOpen (
			htmlClassAttribute ( 
				"manual-responder-request-pending-summary"));

		htmlDivOpen (
			htmlClassAttribute (
				"layout-container"));

		htmlTableOpenLayout ();

		htmlTableRowOpen ();

		htmlTableCellWriteHtml (
			this::goRequestDetails,
			htmlAttribute (
				"style",
				"width: 50%"));

		htmlTableCellWriteHtml (
			() -> {
				goCustomerDetails ();
				goNotes ();
			},
			htmlAttribute (
				"style",
				"width: 50%"));

		htmlTableRowClose ();

		htmlTableClose ();

		htmlDivClose ();

		goBillHistory ();

		goOperatorInfo ();

		goSessionDetails ();

		goRequestHistory ();

		htmlDivClose ();

	}

	void goRequestDetails () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteRaw (
			"Manual responder",
			() -> objectManager.writeTdForObjectMiniLink (
				manualResponder));

		htmlTableDetailsRowWrite (
			"Description",
			manualResponder.getDescription ());

		if (
			privChecker.canRecursive (
				manualResponder,
				"number")
		) {

			htmlTableDetailsRowWriteRaw (
				"Number",
				() -> objectManager.writeTdForObjectMiniLink (
					number));

		}

		htmlTableDetailsRowWriteRaw (
			"Network",
			() -> objectManager.writeTdForObjectMiniLink (
				network));

		htmlTableDetailsRowWriteRaw (
			"Message",
			() -> objectManager.writeTdForObjectMiniLink (
				message));

		htmlTableDetailsRowWrite (
			"Message text",
			message.getText ().getText (),
			htmlClassAttribute (
				"bigger"));

		for (
			MediaRec media
				: message.getMedias ()
		) {

			if (
				mediaLogic.isText (
					media)
			) {

				htmlTableDetailsRowWriteHtml (
					"Text media",
					() -> mediaConsoleLogic.writeMediaContent (
						media));

			} else if (
				mediaLogic.isImage (
					media)
			) {

				htmlTableDetailsRowWriteRaw (
					"Image media",
					() -> htmlLinkWriteHtml (
						mediaConsoleLogic.mediaUrlScaled (
							media,
							600,
							600),
						() -> mediaConsoleLogic.writeMediaThumb100 (
							media)));

			} else {

				htmlTableDetailsRowWrite (
					"Media",
					"(unsupported media type)");

			}

		}

		htmlTableClose ();

	}

	void goCustomerDetails () {

		htmlHeadingThreeWrite (
			"Customer details");

		if (
			isNull (
				smsCustomer)
		) {

			formatWriter.writeLineFormat (
				"<p>Customer management is not configured for this manual ",
				"responder service.</p>");

			return;

		}

		formFieldLogic.outputDetailsTable (
			formatWriter,
			customerDetailsFields,
			smsCustomer,
			ImmutableMap.of ());

	}

	void goNotes () {

		htmlHeadingThreeWrite (
			"Notes");

		String notes;

		if (

			isNotNull (
				manualResponderNumber)

			&& isNotNull (
				manualResponderNumber.getNotesText ())

		) {

			notes =
				manualResponderNumber.getNotesText ().getText ();

		} else if (

			isNotNull (
				smsCustomer)

			&& isNotNull (
				smsCustomer.getNotesText ())

		) {

			notes =
				smsCustomer.getNotesText ().getText ();

		} else {

			notes = "";

		}

		formatWriter.writeLineFormat (
			"<p",
			" id=\"%h\"",
			stringFormat (
				"manualResponderNumberNote%d",
				manualResponderRequest.getNumber ().getId ()),
			" class=\"mrNumberNoteEditable\"",
			">%s</p>",
			HtmlUtils.newlineToBr (
				HtmlUtils.htmlEncode (
					notes)));

	}

	void goBillHistory () {

		if (! manualResponder.getShowDailyBillInfo ())
			return;

		if (routeBillInfos.isEmpty ())
			return;

		htmlHeadingTwoWrite (
			"Bill history for today");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Route",
			"All services",
			"This service");

		for (
			RouteBillInfo routeBillInfo
				: routeBillInfos
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				routeBillInfo.route ().getCode ());

			htmlTableCellWriteHtml (
				currencyLogic.formatHtml (
					manualResponder.getCurrency (),
					routeBillInfo.total ()));

			htmlTableCellWriteHtml (
				currencyLogic.formatHtml (
					manualResponder.getCurrency (),
					routeBillInfo.thisService ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

	void goOperatorInfo () {

		// TODO should be nullable to avoid this...

		if (manualResponder.getInfoText () == null)
			return;

		if (manualResponder.getInfoText ().getText ().length () == 0)
			return;

		htmlHeadingTwoWrite (
			"Operator info");

		formatWriter.writeString (
			manualResponder.getInfoText ().getText ());

	}

	void goSessionDetails () {

		if (smsCustomerSession == null)
			return;

		if (
			smsCustomerSession.getWelcomeMessage () == null
			&& smsCustomerSession.getWarningMessage () == null
		) {
			return;
		}

		htmlHeadingTwoWrite (
			"Customer session");

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Start time",
			userConsoleLogic.timestampWithTimezoneString (
				smsCustomerSession.getStartTime ()));

		if (
			isNotNull (
				smsCustomerSession.getEndTime ())
		) {

			htmlTableDetailsRowWrite (
				"End time",
				userConsoleLogic.timestampWithTimezoneString (
					smsCustomerSession.getStartTime ()));

		}

		if (smsCustomerSession.getWelcomeMessage () != null) {

			htmlTableDetailsRowWrite (
				"Welcome message",
				stringFormat (
					"%s (sent %s)",
					smsCustomerSession
						.getWelcomeMessage ()
						.getText ()
						.getText (),
					userConsoleLogic.timestampWithTimezoneString (
						smsCustomerSession
							.getWelcomeMessage ()
							.getCreatedTime ())));

		}

		if (smsCustomerSession.getWarningMessage () != null) {

			htmlTableDetailsRowWrite (
				"Warning message",
				stringFormat (
					"%s (sent %s)",
					smsCustomerSession
						.getWarningMessage ()
						.getText ()
						.getText (),
					userConsoleLogic.timestampWithTimezoneString (
						smsCustomerSession
							.getWarningMessage ()
							.getCreatedTime ())));

		}

		htmlTableClose ();

	}

	void goRequestHistory () {

		if (oldRequests.isEmpty ())
			return;

		// title

		htmlHeadingTwoWrite (
			"Request history");

		// warning if we have omitted old requests

		if (oldRequests.size () > maxResults) {

			formatWriter.writeLineFormat (
				"<p class=\"warning\">%h</p>",
				stringFormat (
					"Only showing the first %s results.",
					maxResults));

		}

		// begin table

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Timestamp",
			null,
			"Message",
			"Media",
			"User");

		// iterate requests

		for (
			ManualResponderRequestRec oldRequest
				: oldRequests
		) {

			htmlTableRowSeparatorWrite ();

			// iterate replies, which are shown before their requests

			for (
				ManualResponderReplyRec oldReply
					: oldRequest.getReplies ()
			) {

				// print reply

				htmlTableRowOpen (
					htmlClassAttribute (
						"message-out"));

				htmlTableCellWriteHtml (
					"&nbsp;");

				htmlTableCellWriteHtml (
					userConsoleLogic.timestampWithTimezoneString (
						oldReply.getTimestamp ()));

				htmlTableCellWriteHtml (
					oldReply.getText ().getText ());

				htmlTableCellWrite (
					"");

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						oldReply.getUser (),
						() -> oldReply.getUser ().getUsername ()));

				htmlTableCellClose ();

			}

			// print request

			htmlTableRowOpen (
				htmlClassAttribute (
					"message-in"));

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					oldRequest.getTimestamp ()),
				htmlColumnSpanAttribute (2l));

			htmlTableCellWrite (
				oldRequest.getMessage ().getText ().getText ());

			// print request medias

			htmlTableCellWriteHtml (
				() -> oldRequest.getMessage ().getMedias ().forEach (
					media -> {

				if (
					mediaLogic.isImage (
						media)
				) {

					mediaConsoleLogic.writeMediaThumb32 (
						media);

				}

			}));

			// leave request user blank

			htmlTableCellWrite (
				"");

			htmlTableRowClose ();

		}

		// close table

		htmlTableClose ();

	}

	@Accessors (fluent = true)
	@Data
	static
	class RouteBillInfo
		implements Comparable <RouteBillInfo> {

		RouteRec route;

		Long total = 0l;
		Long thisService = 0l;

		@Override
		public
		int compareTo (
				RouteBillInfo other) {

			return new CompareToBuilder ()
				.append (route (), other.route ())
				.toComparison ();

		}

	}

}
