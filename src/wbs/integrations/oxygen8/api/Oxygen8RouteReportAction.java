package wbs.integrations.oxygen8.api;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogType;
import wbs.integrations.oxygen8.model.Oxygen8ReportCodeObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8ReportCodeRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.utils.string.FormatWriter;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@SingletonComponent ("oxygen8RouteReportAction")
public
class Oxygen8RouteReportAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	Oxygen8InboundLogObjectHelper oxygen8InboundLogHelper;

	@SingletonDependency
	Oxygen8ReportCodeObjectHelper oxygen8ReportCodeHelper;

	@SingletonDependency
	Oxygen8RouteOutObjectHelper oxygen8RouteOutCodeHelper;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	Long routeId;

	String reference;
	String status;

	Boolean success = false;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull TaskLogger taskLogger,
			@NonNull FormatWriter debugWriter) {

		routeId =
			requestContext.requestIntegerRequired (
				"routeId");

		reference =
			requestContext.parameterOrNull (
				"Reference");

		status =
			requestContext.parameterOrNull (
				"Status");

	}

	@Override
	protected
	void updateDatabase (
			@NonNull TaskLogger taskLogger) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s ()",
					getClass ().getSimpleName (),
					"updateDatabase"),
				this);

		Oxygen8RouteOutRec routeOut =
			oxygen8RouteOutCodeHelper.findRequired (
				routeId);

		if (! routeOut.getRoute ().getDeliveryReports ()) {

			throw new RuntimeException (
				stringFormat (
					"Delivery reports are not enabled for route %s.%s",
					routeOut.getRoute ().getSlice ().getCode (),
					routeOut.getRoute ().getCode ()));

		}

		Oxygen8ReportCodeRec reportCode =
			oxygen8ReportCodeHelper.findByCodeRequired (
				routeOut.getOxygen8Config (),
				status);

		RouteRec route =
			smsRouteHelper.findRequired (
				routeId);

		reportLogic.deliveryReport (
			route,
			reference,
			reportCode.getMessageStatus (),
			optionalOf (
				status),
			optionalOf (
				reportCode.getDescription ()),
			optionalAbsent (),
			optionalAbsent ());

		transaction.commit ();

		success = true;

	}

	@Override
	protected
	Responder createResponse (
			@NonNull TaskLogger taskLogger,
			@NonNull FormatWriter debugWriter) {

		// encode response

		String responseString = "SUCCESS\n";

		// write to debug log

		debugWriter.writeString (
			"== RESPONSE BODY ==\n\n");

		debugWriter.writeString (
			responseString);

		debugWriter.writeString (
			"\n\n");

		// create responder

		return textResponderProvider.get ()

			.contentType (
				"text/plain")

			.text (
				responseString);

	}

	@Override
	protected
	void storeLog (
			@NonNull TaskLogger taskLogger,
			@NonNull String debugLog) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s ()",
					getClass ().getSimpleName (),
					"storeLog"),
				this);

		oxygen8InboundLogHelper.insert (
			oxygen8InboundLogHelper.createInstance ()

			.setRoute (
				smsRouteHelper.findRequired (
					routeId))

			.setType (
				Oxygen8InboundLogType.smsDelivery)

			.setTimestamp (
				transaction.now ())

			.setDetails (
				debugLog)

			.setSuccess (
				success)

		);

		transaction.commit ();

	}

}
