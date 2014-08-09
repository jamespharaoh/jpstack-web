package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
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

import com.google.common.collect.ImmutableSet;

@Log4j
@PrototypeComponent ("manualResponderRequestPendingSummaryPart")
public
class ManualResponderRequestPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ManualResponderRequestRec manualResponderRequest;
	ManualResponderNumberRec manualResponderNumber;
	ManualResponderRec manualResponder;

	NumberRec number;
	NetworkRec network;

	List<ManualResponderRequestRec> oldManualResponderRequests;
	List<RouteBillInfo> routeBillInfos;

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/jquery-1.7.1.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/jquery.jeditable.mini.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/manual-responder-pending-summary.js"))

			.build ();

	}

	@Override
	public
	void prepare () {

		manualResponderRequest =
			manualResponderRequestHelper.find (
				requestContext.stuffInt ("manualResponderRequestId"));

		manualResponder =
			manualResponderRequest.getManualResponder ();

		number =
			manualResponderRequest.getNumber ();

		network =
			number.getNetwork ();

		manualResponderNumber =
			manualResponderNumberHelper.find (
				manualResponder,
				number);

		// get routes

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

			for (MessageRec message
					: messageHelper.search (
						new MessageSearch ()
							.numberId (number.getId ())
							.routeId (route.getId ())
							.createdTimeAfter (startOfToday.toDate ())
							.direction (MessageDirection.out))) {

				if (
					in (message.getStatus (),
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

		oldManualResponderRequests =
			manualResponderRequestHelper.find (
				manualResponder,
				manualResponderRequest.getNumber ());

		Collections.sort (
			oldManualResponderRequests);

	}

	@Override
	public
	void goBodyStuff () {

		if (manualResponderRequest == null) {

			printFormat (
				"<p>Not found</p>\n");

			return;

		}

		goSummary ();

		goBillHistory ();

		goNotes ();

		goOperatorInfo ();

		goRequestHistory ();

	}

	void goSummary () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>ID</th>\n",
			"<td>%h</td>\n",
			manualResponderRequest.getId (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Manual responder</th>\n",
			"%s\n",
			objectManager.tdForObject (
				manualResponder,
				null,
				true,
				true),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Description</th>\n",
			"<td>%h</td>\n",
			manualResponder.getDescription (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Number</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				number),
			"</tr>\n");

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
			objectManager.tdForObject (
				manualResponderRequest.getMessage (),
				null,
				true,
				true),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message text</th>\n",
			"<td class=\"bigger\">%h</td>\n",
			manualResponderRequest.getMessage ().getText ().getText (),
			"</tr>\n");

		printFormat (
			"</table>\n");

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
					routeBillInfo.total ()),

				"<td>%s</td>\n",
				currencyLogic.formatHtml (
					manualResponder.getCurrency (),
					routeBillInfo.thisService ()),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	void goNotes () {

		printFormat (
			"<h2>Notes</h2>\n");

		printFormat (
			"<p",
			" id=\"%h\"",
			stringFormat (
				"manualResponderNumberNote%d",
				manualResponderRequest.getNumber ().getId ()),
			" class=\"mrNumberNoteEditable\"",
			">%s</p>\n",
			Html.newlineToBr (Html.encode (
				manualResponderNumber != null &&
				manualResponderNumber.getNotesText () != null
					? manualResponderNumber.getNotesText ().getText ()
					: "")));

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

	void goRequestHistory () {

		if (oldManualResponderRequests.isEmpty ())
			return;

		printFormat (
			"<h2>Request history</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th colspan=\"2\">Timestamp</th>\n",
			"<th>Message</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (ManualResponderRequestRec oldManualResponderRequest
				: oldManualResponderRequests) {

			printFormat (
				"<tr class=\"sep\">\n");

			for (ManualResponderReplyRec oldReply
					: oldManualResponderRequest.getReplies ()) {

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
							dateToInstant (oldReply.getTimestamp ())),
						"-"));

				printFormat (
					"<td>%h</td>\n",
					oldReply.getText ().getText ());

				printFormat (
					"<td>%h</td>\n",
					oldReply.getUser ().getUsername ());

				printFormat (
					"</tr>\n");

			}

			printFormat (
				"<tr",
				" class=\"message-in\"",
				">\n");

			printFormat (
				"<td colspan=\"2\">%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (oldManualResponderRequest.getTimestamp ())));

			printFormat (
				"<td>%h</td>\n",
				oldManualResponderRequest.getMessage ().getText ().getText ());

			printFormat (
				"<td>%h</td>\n",
				oldManualResponderRequest.getUser () != null
					? oldManualResponderRequest.getUser ().getUsername ()
					: "");

			printFormat (
				"</tr>\n");

		}

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
