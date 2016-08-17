package wbs.integrations.mig.api;

import static wbs.framework.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.framework.utils.etc.StringUtils.joinWithNewline;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.integrations.mig.logic.MigLogic;
import wbs.integrations.mig.model.MigRouteInObjectHelper;
import wbs.integrations.mig.model.MigRouteInRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
public
class MigApiServletModule
	implements ServletModule {

	// dependencies

	@Inject
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionUtils exceptionLogic;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	SmsMessageLogic messageLogic;

	@Inject
	MigLogic migLogic;

	@Inject
	MigRouteInObjectHelper migRouteInHelper;

	@Inject
	SmsDeliveryReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

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
				database.beginReadWrite (
					"MigApiServletModule.inFile.doGet ()",
					this);

			try {

				// get request stuff

				Long routeId =
					requestContext.requestIntegerRequired (
						"routeId");

				// get params in local variables
				// String ifVersion = requestContext.getParameter ("IFVERSION");
				// String messageType = requestContext.getParameter ("MESSAGETYPE");
				String oadc = requestContext.parameterOrNull("OADC");

				String guidParam =
					requestContext.parameterOrNull (
						"GUID");

				// String receiveTime = requestContext.getParameter ("RECEIVETIME");
				String body = requestContext.parameterOrNull("BODY");
				// String mclass = requestContext.getParameter ("MCLASS");
				// String header = requestContext.getParameter ("HEADER");
				String destAddress = requestContext.parameterOrNull("DESTADDRESS");
				String connection = requestContext.parameterOrNull("CONNECTION");
				String avStatus = requestContext.parameterOrNull("AVSTATUS");
				// String dcs = requestContext.getParameter ("DCS");
				// String retryCount = requestContext.getParameter ("RETRYCOUNT");

				MigRouteInRec migRouteIn =
					migRouteInHelper.findOrThrow (
						routeId,
						() -> new RuntimeException (
							stringFormat (
								"No mig inbound route information for %s",
								routeId)));

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
					routeHelper.findRequired (
						routeId);

				// insert the message

				MessageRec message =
					smsInboxLogic.inboxInsert (
						Optional.of (
							guidParam),
						textHelper.findOrCreate (
							body),
						oadc,
						destAddress,
						route,
						migRouteIn.getSetNetwork ()
							? Optional.of (
								network)
							: Optional.absent (),
						Optional.absent (),
						Collections.emptyList (),
						Optional.of (
							avStatus),
						Optional.absent ());

				transaction.commit ();

				String response = "000";
				PrintWriter out = requestContext.writer ();
				out.println (response);

				log.debug (
					"Response 000 " + message.getId ());

			} catch (Exception exception) {

				exceptionLogger.logSimple (
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						exception),
					getException (exception, requestContext),
					Optional.absent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

				PrintWriter out =
					requestContext.writer ();

				out.println (
					"400");

				log.debug (
					"Response 400 ");

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
				database.beginReadWrite (
					"MigApiServletModule.reportFile.doGet ()",
					this);

			try {

				// get request stuff

				Long routeId =
					requestContext.requestIntegerRequired (
						"routeId");

				// String ifVersion = requestContext.getParameter ("IFVERSION");
				String statusType = requestContext.parameterOrNull("STATUSTYPE");
				String guid = requestContext.parameterOrNull("GUID");
				String messageID = requestContext.parameterOrNull("MESSAGEID");
				// String statusTime = requestContext.getParameter ("STATUSTIME");
				// String dischargeTime = requestContext.getParameter ("DISCHARGETIME");
				String status = requestContext.parameterOrNull("STATUS");
				String reason = requestContext.parameterOrNull("REASONCODE");
				String description = requestContext.parameterOrNull("DESCRIPTION");
				String destAddress = requestContext.parameterOrNull("DESTADDRESS");
				String connection = requestContext.parameterOrNull("CONNECTION");
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
					routeHelper.findRequired (
						routeId);

				MessageRec message;

				if (messageID != null) {

					message =
						messageHelper.findOrThrow (
							Long.parseLong (
								messageID),
							() -> new NoSuchMessageException (
								"Message ID: " + messageID));

				} else if (guid != null) {

					message =
						messageHelper.findByOtherId (
							MessageDirection.out,
							route,
							guid);

				} else {

					throw new RuntimeException (
						"No message id or guid");

				}

				reportLogic.deliveryReport (
					message,
					newMessageStatus,
					Optional.of (
						status),
					optionalFromNullable (
						description),
					Optional.of (
						joinWithNewline (
							stringFormat (
								"status=%s",
								status),
							stringFormat (
								"statusType=%s",
								statusType),
							stringFormat (
								"reason=%s",
								reason))),
					Optional.absent ());

				transaction.commit ();

				String response = "000";
				PrintWriter out = requestContext.writer();
				out.println(response);

				log.debug ("Response 000 " + message.getId());

			} catch (Exception exception) {

				exceptionLogger.logSimple (
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						exception),
					getException (
						exception,
						requestContext),
					Optional.absent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

				PrintWriter out =
					requestContext.writer ();

				out.println (
					"400");

				log.debug (
					"Response 400");

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
				@NonNull Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (
					matcher.group (1)));

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
