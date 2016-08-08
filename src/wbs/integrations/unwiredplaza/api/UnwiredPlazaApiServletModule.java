package wbs.integrations.unwiredplaza.api;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;

import lombok.Cleanup;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.exception.logic.ExceptionLogLogic;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("unwiredPlazaApiServletModule")
public
class UnwiredPlazaApiServletModule
	implements ServletModule {

	// ============================================================ properties

	@Inject
	Database database;

	@Inject
	ExceptionLogLogic exceptionLogic;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
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
		void doGet () {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"UnwiredPlazaApiServletModule.reportFile.doGet ()",
					this);

			int routeId =
				requestContext.requestIntRequired (
					"routeId");

			Integer id =
				Integer.parseInt (requestContext.parameterOrNull ("id"));

			Long status =
				Long.parseLong (
					requestContext.parameterOrNull (
						"status"));

			Long subStatus =
				Long.parseLong (
					requestContext.parameterOrNull (
						"substatus"));

			Long finalParam =
				Long.parseLong (
					requestContext.parameterOrNull (
						"final"));

			//String phone =
			//	requestContext.getParameter ("phone");

			//String refid =
			//	requestContext.getParameter ("refid");

			//Integer charged =
			//	Integer.parseInt (requestContext.getParameter ("charged"));

			String statusCode =
				statusCodes.get (status);

			String subStatusCode =
				subStatusCodes.get (subStatus);

			MessageStatus result =
				statusResults.get (status);

			RouteRec route =
				routeHelper.findRequired (
					routeId);

			// update message report code

			MessageReportCodeRec messageReportCode =
				messageReportCodeHelper.findOrCreate (
					status,
					null,
					subStatus,
					MessageReportCodeType.unwiredPlaza,
					result != null
						? result.isGoodType ()
						: false,
					finalParam == 1,
					stringFormat (
						"%s / %s",
						statusCode,
						subStatusCode));

			// process delivery report

			reportLogic.deliveryReport (
				route,
				id.toString (),
				result,
				null,
				messageReportCode);

			transaction.commit ();

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
