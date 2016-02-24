package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.log4j.Level;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.framework.utils.etc.ProfileLogger;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
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

@Log4j
@PrototypeComponent ("manualResponderRequestPendingSummaryPart")
public
class ManualResponderRequestPendingSummaryPart
	extends AbstractPagePart {

	final
	int maxResults = 1000;

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject @Named
	ConsoleModule manualResponderRequestPendingConsoleModule;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	PrivChecker privChecker;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	@Inject
	TimeFormatter timeFormatter;

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

		ProfileLogger profileLogger =
			new ProfileLogger (
				log,
				Level.INFO,
				"prepare");

		profileLogger.lap (
			"load basics");

		manualResponderRequest =
			manualResponderRequestHelper.find (
				requestContext.stuffInt (
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
				? smsCustomerSessionHelper.findByIndex (
					smsCustomer,
					smsCustomer.getNumSessions () - 1)
				: null;

		// get routes

		profileLogger.lap (
			"load routes");

		Set<RouteRec> routes =
			new HashSet<RouteRec> ();

		for (ManualResponderTemplateRec manualResponderTemplate
				: manualResponder.getTemplates ()) {

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

		profileLogger.lap (
			"load route bill counts");

		Instant startOfToday =
			LocalDate.now ()
				.toDateTimeAtStartOfDay ()
				.toInstant ();

		ServiceRec defaultService =
			serviceHelper.findByCode (
				manualResponder,
				"default");

		routeBillInfos =
			new ArrayList<RouteBillInfo> ();

		for (RouteRec route : routes) {

			int total = 0;
			int thisService = 0;

			MessageSearch messageSearch =
				new MessageSearch ()

				.numberId (
					number.getId ())

				.routeId (
					route.getId ())

				.createdTimeAfter (
					dateToInstant (
						startOfToday.toDate ()))

				.direction (
					MessageDirection.out);

			List<MessageRec> messages =
				messageHelper.search (
					messageSearch);

			for (MessageRec message
					: messages) {

				if (
					in (
						message.getStatus (),
						MessageStatus.undelivered,
						MessageStatus.cancelled,
						MessageStatus.reportTimedOut)
				) {

					continue;

				}

				if (
					! in (
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
					equal (
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

		profileLogger.lap (
			"load request history");

		oldRequests =
			manualResponderRequestHelper.findRecentLimit (
				manualResponder,
				manualResponderRequest.getNumber (),
				maxResults + 1);

		Collections.sort (
			oldRequests);

		profileLogger.end ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (manualResponderRequest == null) {

			printFormat (
				"<p>Not found</p>\n");

			return;

		}

		printFormat (
			"<div class=\"manual-responder-request-pending-summary\">\n");

		printFormat (
			"<div class=\"layout-container\">\n",
			"<table class=\"layout\">\n",
			"<tbody>\n",
			"<tr>\n",
			"<td style=\"width: 50%%\">\n");

		goRequestDetails ();

		printFormat (
			"</td>\n",
			"<td style=\"width: 50%%\">\n");

		goCustomerDetails ();
		goNotes ();

		printFormat (
			"</td>\n",
			"</tr>\n",
			"</tbody>\n",
			"</table>\n",
			"</div>\n");

		goBillHistory ();

		goOperatorInfo ();

		goSessionDetails ();

		goRequestHistory ();

		printFormat (
			"</div>\n");

	}

	void goRequestDetails () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Manual responder</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				manualResponder),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Description</th>\n",
			"<td>%h</td>\n",
			manualResponder.getDescription (),
			"</tr>\n");

		if (
			privChecker.can (
				manualResponder,
				"number")
		) {

			printFormat (
				"<tr>\n",
				"<th>Number</th>\n",
				"%s\n",
				objectManager.tdForObjectMiniLink (
					number),
				"</tr>\n");

		}

		printFormat (
			"<tr>\n",
			"<th>Network</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				network),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				message),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message text</th>\n",
			"<td class=\"bigger\">%h</td>\n",
			message.getText ().getText (),
			"</tr>\n");

		for (
			MediaRec media
				: message.getMedias ()
		) {

			if (
				mediaLogic.isText (
					media)
			) {

				printFormat (
					"<tr>\n",
					"<th>Text media</th>\n",
					"<td>%h</td>\n",
					mediaConsoleLogic.mediaContent (
						media),
					"</tr>\n");

			} else if (
				mediaLogic.isImage (
					media)
			) {

				printFormat (
					"<tr>\n",
					"<th>Image media</th>\n",
					"<td><a href=\"%h\">%s</a></td>\n",
					mediaConsoleLogic.mediaUrlScaled (
						media,
						600,
						600),
					mediaConsoleLogic.mediaThumb100 (
						media),
					"</tr>\n");

			} else {

				printFormat (
					"<tr>\n",
					"<th>Media</th>\n",
					"<td>(unsupported media type)</td>\n",
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

	void goCustomerDetails () {

		printFormat (
			"<h3>Customer details</h3>\n");

		if (
			isNull (
				smsCustomer)
		) {

			printFormat (
				"<p>Customer management is not configured for this ",
				"manual responder service.</p>\n");

			return;

		}

		formFieldLogic.outputDetailsTable (
			formatWriter,
			customerDetailsFields,
			smsCustomer,
			ImmutableMap.<String,Object>of ());

	}

	void goNotes () {

		printFormat (
			"<h3>Notes</h3>\n");

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

		printFormat (
			"<p",
			" id=\"%h\"",
			stringFormat (
				"manualResponderNumberNote%d",
				manualResponderRequest.getNumber ().getId ()),
			" class=\"mrNumberNoteEditable\"",
			">%s</p>\n",
			Html.newlineToBr (
				Html.encode (
					notes)));

	}

	void goBillHistory () {

		if (! manualResponder.getShowDailyBillInfo ())
			return;

		if (routeBillInfos.isEmpty ())
			return;

		printFormat (
			"<h2>Bill history for today</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Route</th>\n",
			"<th>All services</th>\n",
			"<th>This service</th>\n",
			"</tr>\n");

		for (RouteBillInfo routeBillInfo
				: routeBillInfos) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				routeBillInfo.route ().getCode (),

				"<td>%s</td>\n",
				currencyLogic.formatHtml (
					manualResponder.getCurrency (),
					Long.valueOf(routeBillInfo.total ())),

				"<td>%s</td>\n",
				currencyLogic.formatHtml (
					manualResponder.getCurrency (),
					Long.valueOf(routeBillInfo.thisService ())),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	void goOperatorInfo () {

		// TODO should be nullable to avoid this...

		if (manualResponder.getInfoText () == null)
			return;

		if (manualResponder.getInfoText ().getText ().length () == 0)
			return;

		printFormat (
			"<h2>Operator info</h2>\n");

		printFormat (
			"%s\n",
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

		printFormat (
			"<h2>Customer session</h2>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Start time</th>\n",
			"<td>%h</td>\n",
			timeFormatter.instantToTimestampString (
				timeFormatter.defaultTimezone (),
				smsCustomerSession.getStartTime ()),
			"</tr>\n");

		if (smsCustomerSession.getEndTime () != null) {

			printFormat (
				"<tr>\n",
				"<th>End time</th>\n",
				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					smsCustomerSession.getStartTime ()),
				"</tr>\n");

		}

		if (smsCustomerSession.getWelcomeMessage () != null) {

			printFormat (
				"<tr>\n",
				"<th>Welcome message</th>\n",
				"<td>%h (sent %h)</td>\n",
				smsCustomerSession
					.getWelcomeMessage ()
					.getText ()
					.getText (),
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						smsCustomerSession
							.getWelcomeMessage ()
							.getCreatedTime ())),
				"</tr>\n");

		}

		if (smsCustomerSession.getWarningMessage () != null) {

			printFormat (
				"<tr>\n",
				"<th>Warning message</th>\n",
				"<td>%h (sent %h)</td>\n",
				smsCustomerSession
					.getWarningMessage ()
					.getText ()
					.getText (),
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						smsCustomerSession
							.getWarningMessage ()
							.getCreatedTime ())),
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	void goRequestHistory () {

		if (oldRequests.isEmpty ())
			return;

		// title

		printFormat (
			"<h2>Request history</h2>\n");

		// warning if we have omitted old requests

		if (oldRequests.size () > maxResults) {

			printFormat (
				"<p class=\"warning\">%h</p>\n",
				stringFormat (
					"Only showing the first %s results.",
					maxResults));

		}

		// begin table

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th colspan=\"2\">Timestamp</th>\n",
			"<th>Message</th>\n",
			"<th>Media</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		// iterate requests

		for (
			ManualResponderRequestRec oldRequest
				: oldRequests
		) {

			printFormat (
				"<tr class=\"sep\">\n");

			// iterate replies, which are shown before their requests

			for (
				ManualResponderReplyRec oldReply
					: oldRequest.getReplies ()
			) {

				// print reply

				printFormat (
					"<tr",
					" class=\"message-out\"",
					">\n");

				printFormat (
					"<td>&nbsp;</td>\n");

				printFormat (
					"<td>%s</td>\n",
					ifNull (
						timeFormatter.instantToTimestampString (
							timeFormatter.defaultTimezone (),
							dateToInstant (
								oldReply.getTimestamp ())),
						"-"));

				printFormat (
					"<td>%h</td>\n",
					oldReply.getText ().getText ());

				printFormat (
					"<td></td>\n");

				printFormat (
					"<td>%h</td>\n",
					oldReply.getUser () != null
						? oldReply.getUser ().getUsername ()
						: "-");

				printFormat (
					"</tr>\n");

			}

			// print request

			printFormat (
				"<tr",
				" class=\"message-in\"",
				">\n");

			printFormat (
				"<td colspan=\"2\">%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						oldRequest.getTimestamp ())));

			printFormat (
				"<td>%h</td>\n",
				oldRequest
					.getMessage ()
					.getText ()
					.getText ());

			// print request medias

			printFormat (
				"<td>\n");

			for (
				MediaRec media
					: oldRequest.getMessage ().getMedias ()
			) {

				if (
					mediaLogic.isImage (
						media)
				) {

					printFormat (
						"%s\n",
						mediaConsoleLogic.mediaThumb32 (
							media));

				}

			}

			printFormat (
				"</td>\n");

			// print request user

			printFormat (
				"<td>%h</td>\n",
				oldRequest.getUser () != null
					? oldRequest.getUser ().getUsername ()
					: "");

			printFormat (
				"</tr>\n");

		}

		// close table

		printFormat (
			"</table>\n");

	}

	@Accessors (fluent = true)
	@Data
	static
	class RouteBillInfo
		implements Comparable<RouteBillInfo> {

		RouteRec route;

		Integer total = 0;
		Integer thisService = 0;

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
