package wbs.integrations.mig.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.integrations.mig.logic.MigLogic;
import wbs.integrations.mig.model.MigRouteInObjectHelper;
import wbs.integrations.mig.model.MigRouteInRec;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Log4j
public
class MigApiServletModule
	implements ServletModule {

	@Inject
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	MigLogic migLogic;

	@Inject
	MigRouteInObjectHelper migRouteInHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	private
	String getException (
			Throwable throwable,
			RequestContext requestContext) {

		// TODO something better about this

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			exceptionLogic.throwableDump (
				throwable));

		stringBuilder.append (
			"\n\nHTTP INFO\n\n");

		stringBuilder.append (
			"METHOD = " + requestContext.method () + "\n\n");

		for (Map.Entry<String,List<String>> entry
				: requestContext.parameterMap ().entrySet ()) {

			for (String value
				: entry.getValue ()) {

				stringBuilder.append (
					entry.getKey ());

				stringBuilder.append (
					" = \"");

				stringBuilder.append (
					value);

				stringBuilder.append (
					"\"\n");

			}

		}

		return stringBuilder.toString ();

	}

	WebFile inFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			doGet ();

		}

		@Override
		public
		void doGet ()
			throws
				ServletException,
				IOException {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			try {

				// get request stuff

				int routeId =
					requestContext.requestInt ("routeId");

				// get params in local variables
				// String ifVersion = requestContext.getParameter ("IFVERSION");
				// String messageType = requestContext.getParameter ("MESSAGETYPE");
				String oadc = requestContext.parameter("OADC");
				String messageID = requestContext.parameter("GUID");
				// String receiveTime = requestContext.getParameter ("RECEIVETIME");
				String body = requestContext.parameter("BODY");
				// String mclass = requestContext.getParameter ("MCLASS");
				// String header = requestContext.getParameter ("HEADER");
				String destAddress = requestContext.parameter("DESTADDRESS");
				String connection = requestContext.parameter("CONNECTION");
				String avStatus = requestContext.parameter("AVSTATUS");
				// String dcs = requestContext.getParameter ("DCS");
				// String retryCount = requestContext.getParameter ("RETRYCOUNT");

				MigRouteInRec migRouteIn =
					migRouteInHelper.find (
						routeId);

				if (migRouteIn == null) {

					throw new RuntimeException (
						"No mig inbound route information for " + routeId);

				}

				NetworkRec network =
					migLogic.getNetwork (
						connection,
						destAddress);

				if (oadc.startsWith ("00")) {

					oadc =
						oadc.substring (
							2,
							oadc.length ());

				}

				RouteRec route =
					routeHelper.find (routeId);

				// insert the message

				MessageRec message =
					inboxLogic.inboxInsert (
						Optional.of (messageID),
						textHelper.findOrCreate (body),
						oadc,
						destAddress,
						route,
						migRouteIn.getSetNetwork ()
							? Optional.of (network)
							: Optional.<NetworkRec>absent (),
						Optional.<Instant>absent (),
						Collections.<MediaRec>emptyList (),
						Optional.of (avStatus),
						Optional.<String>absent ());

				transaction.commit ();

				String response = "000";
				PrintWriter out = requestContext.writer ();
				out.println (response);

				log.debug ("Response 000 " + message.getId ());

			} catch (Exception exception) {

				exceptionLogic.logSimple (
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						exception),
					getException (exception, requestContext),
					Optional.<Integer>absent (),
					false);

				PrintWriter out =
					requestContext.writer ();

				out.println ("400");

				log.debug ("Response 400 ");

			}

		}

	};

	private
	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			doGet ();

		}

		@Override
		public
		void doGet ()
			throws
				ServletException,
				IOException {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			try {

				// get request stuff

				int routeId =
					requestContext.requestInt ("routeId");

				// String ifVersion = requestContext.getParameter ("IFVERSION");
				String statusType = requestContext.parameter("STATUSTYPE");
				String guid = requestContext.parameter("GUID");
				String messageID = requestContext.parameter("MESSAGEID");
				// String statusTime = requestContext.getParameter ("STATUSTIME");
				// String dischargeTime = requestContext.getParameter ("DISCHARGETIME");
				String status = requestContext.parameter("STATUS");
				String reason = requestContext.parameter("REASONCODE");
				String description = requestContext.parameter("DESCRIPTION");
				String destAddress = requestContext.parameter("DESTADDRESS");
				String connection = requestContext.parameter("CONNECTION");
				// String retryCount = requestContext.getParameter ("RETRYCOUNT");

				NetworkRec network =
					migLogic.getNetwork (connection, destAddress);

				MessageStatus newMessageStatus = null;

				// not three
				if (network.getId () != 6) {
					if (Integer.parseInt(status) == 0
							&& Integer.parseInt(statusType) == 20) {
						newMessageStatus = MessageStatus.delivered;
					} else {
						newMessageStatus = MessageStatus.undelivered;
					}
				} else {
					// delivered and billed
					if (Integer.parseInt(status) == 0
							&& Integer.parseInt(statusType) == 0) {
						newMessageStatus = MessageStatus.delivered;
					}
					// delivered and billed
					else if (Integer.parseInt(status) == 0
							&& Integer.parseInt(statusType) == 20) {
						newMessageStatus = MessageStatus.delivered;
					}
					// not delivered but billed
					else if (Integer.parseInt(status) != 0
							&& Integer.parseInt(statusType) == 20) {
						newMessageStatus = MessageStatus.delivered;
					}
					// anything else
					else {
						newMessageStatus = MessageStatus.undelivered;
					}
				}

				// lookup the message

				RouteRec route =
					routeHelper.find (
						routeId);

				MessageRec message = null;

				if (messageID != null) {

					message =
						messageHelper.find (
							Integer.parseInt (messageID));

				} else if (guid != null) {

					message =
						messageHelper.findByOtherId (
							MessageDirection.out,
							route,
							guid);

				}

				if (message == null) {

					throw new NoSuchMessageException (
						"Message ID: " + messageID);

				}

				int statusInt = Integer.parseInt (status);

				Integer statusTypeInt = null;
				try {
					statusTypeInt = Integer.parseInt (statusType);
				} catch (NumberFormatException e) {
					statusTypeInt = null;
				}

				Integer reasonInt;
				try {
					reasonInt = Integer.parseInt(reason);
				} catch (NumberFormatException e) {
					reasonInt = null;
				}

				MessageReportCodeRec reportCode =
					messageReportCodeHelper.findOrCreate (
						statusInt,
						statusTypeInt,
						reasonInt,
						MessageReportCodeType.mig,
						newMessageStatus == MessageStatus.delivered,
						false,
						description);

				reportLogic.deliveryReport (
					message,
					newMessageStatus,
					null,
					reportCode);

				// error handling
				// int netID = networkID;
				// if (netID==5) netID=3;

				// chat code, needs moving elsewhere

				if (messageLogic.isChatMessage (message)
						&& message.getCharge () > 0) {

					ChatUserNumberReportRec numberReportRec =
						chatUserNumberReportHelper.find (
							message.getNumber ().getId ());

					// undelivered
					if (newMessageStatus == MessageStatus.undelivered) {
						// update chat user permanent failure
						if (!reportCode.getSuccess()
								&& reportCode.getPermanent()
								&& ! (network.getId () == 6 && statusInt == 5
										&& description != null && description
										.contains("credit"))) {
							numberReportRec
									.setPermanentFailureReceived(new Date());
							numberReportRec
									.setPermanentFailureCount(numberReportRec
											.getPermanentFailureCount() + 1);
						}
					}
					// delivered
					else if (newMessageStatus == MessageStatus.delivered) {
						numberReportRec.setPermanentFailureReceived(null);
						numberReportRec.setPermanentFailureCount(0);
					}
				}

				transaction.commit();

				String response = "000";
				PrintWriter out = requestContext.writer();
				out.println(response);

				log.debug ("Response 000 " + message.getId());

			} catch (Exception exception) {

				exceptionLogic.logSimple (
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						exception),
					getException (
						exception,
						requestContext),
					Optional.<Integer>absent (),
					false);

				PrintWriter out =
					requestContext.writer ();

				out.println("400");

				log.debug ("Response 400 ");

			}

		}

	};

	final
	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return defaultFiles.get (
				matcher.group (2));

		}

	};

	final
	RegexpPathHandler.Entry inEntry =
		new RegexpPathHandler.Entry (
			"/in/([0-9]+)") {

		@Override
		protected WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return inFile;

		}

	};

	// =========================================================== path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (
			routeEntry,
			inEntry);

	// ================================================================== files

	final
	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("report", reportFile)
			.put ("in", inFile)
			.build ();

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

			.put (
				"/mig",
				pathHandler)

			.build ();

	}

}
