package wbs.integrations.unwiredplaza.api;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.exception.logic.ExceptionLogLogic;

import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("unwiredPlazaApiServletModule")
public
class UnwiredPlazaApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogLogic exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	// ============================================================ report file

	public final static
	Map<Integer,String> statusCodes =
		ImmutableMap.<Integer,String>builder ()
			.put (0, "SMS inited")
			.put (1, "Failed")
			.put (2, "Buffered")
			.put (3, "Acknowledge")
			.put (4, "Delivery failed")
			.put (5, "Delivery success")
			.put (6, "Phone buffered")
			.put (7, "SMSC buffered")
			.put (8, "Sent by OTHERS")
			.put (9, "Sent without DLR")
			.put (20, "Rejected")
			.build ();

	public final static
	Map<Integer,MessageStatus> statusResults =
		ImmutableMap.<Integer,MessageStatus>builder ()
			.put (1, MessageStatus.failed)
			.put (4, MessageStatus.undelivered)
			.put (5, MessageStatus.delivered)
			.put (8, MessageStatus.submitted)
			.put (9, MessageStatus.submitted)
			.put (20, MessageStatus.failed)
			.build ();

	public final static
	Map<Integer,String> subStatusCodes =
		ImmutableMap.<Integer,String>builder ()
			.put (0, "Unknown")
			.put (1, "Couldn't get a route for this message.")
			.put (2, "Couldn't handle SMS at server. No aggregator setting.")
			.put (3, "Connection to aggregator failed.")
			.put (4, "Unkown network.")
			.put (5, "SMS type not allowed by client.")
			.put (6, "BAD UDH settings.")
			.put (7, "Bad URL settings for client (interpret or delivery) for GET action.")
			.put (8, "Bad URL settings for client (interpret or delivery) for XML action.")
			.put (9, "Couldn't read resource for SMS.")
			.put (10, "Permanent operator error.")
			.put (11, "Absent subscriber permanent.")
			.put (12, "Permanent phone related error.")
			.put (13, "Anti-spam.")
			.put (14, "Content related error.")
			.put (15, "Subscriber unable to be billed.")
			.put (16, "Age verification failure - parental lock.")
			.put (17, "Age verification failure - failed AV.")
			.put (18, "The delivering network did not recognise the message type of content.")
			.put (19, "There was an error with the message, probably caused by the content of the message itself.")
			.put (20, "Client cancelled the messaeg by setting the validity period, or the message was terminated by an internal mechanism.")
			.put (21, "An error occurred delivering the message to the handset.")
			.put (22, "The routing gateway or network has had an error routing the message.")
			.put (23, "The message cannot be delivered due to a lack of funds in your accounts.")
			.put (24, "Error delivering message to handset.")
			.put (25, "Error delivering message to handset.")
			.put (26, "User barred, Invalid SMS.")
			.put (27, "Phone reached limit of units.")
			.build ();

	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doGet (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTransaction transaction =
					database.beginReadWriteWithoutParameters (
						logContext,
						parentTaskLogger,
						"reportFile.doGet");

			) {

				// process request

				Long routeId =
					requestContext.requestIntegerRequired (
						"routeId");

				String idParam =
					requestContext.parameterRequired (
						"id");

				Integer id =
					Integer.parseInt (
						idParam);

				String statusParam =
					requestContext.parameterRequired (
						"status");

				String subStatusParam =
					requestContext.parameterRequired (
						"status");

				String finalParam =
					requestContext.parameterRequired (
						"final");

				Long status =
					Long.parseLong (
						statusParam);

				Long subStatus =
					Long.parseLong (
						subStatusParam);

				Long finalValue =
					Long.parseLong (
						finalParam);

				String statusCode =
					statusCodes.get (
						status);

				String subStatusCode =
					subStatusCodes.get (
						subStatus);

				MessageStatus result =
					statusResults.get (
						status);

				// lookup objects

				RouteRec route =
					routeHelper.findRequired (
						transaction,
						routeId);

				// process delivery report

				reportLogic.deliveryReport (
					transaction,
					route,
					id.toString (),
					result,
					Optional.of (
						statusParam),
					Optional.of (
						stringFormat (
							"%s — %s",
							statusCode,
							subStatusCode)),
					Optional.of (
						joinWithSpace (
							stringFormat (
								"status = %s",
								statusParam),
							stringFormat (
								"substatus = %s",
								subStatusParam),
							stringFormat (
								"final = %s",
								integerToDecimalString (
									finalValue)))),
					Optional.absent ());

				transaction.commit ();

			}

		}

	};

	// ================================= entries

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

	// ================================= path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (routeEntry);

	// ============================================================ files

	final
	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("report", reportFile)
			.build ();

	// ============================================================ servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()
			.put ("/unwiredplaza", pathHandler)
			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()
			.build ();

	}
}
