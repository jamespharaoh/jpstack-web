package wbs.integrations.oxygenate.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygenate.model.OxygenateInboundLogObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateInboundLogType;
import wbs.integrations.oxygenate.model.OxygenateReportCodeObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateReportCodeRec;
import wbs.integrations.oxygenate.model.OxygenateRouteOutObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteOutRec;

import wbs.platform.text.web.TextResponder;

import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("oxygenateRouteReportAction")
public
class OxygenateRouteReportAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OxygenateInboundLogObjectHelper oxygenateInboundLogHelper;

	@SingletonDependency
	OxygenateReportCodeObjectHelper oxygenateReportCodeHelper;

	@SingletonDependency
	OxygenateRouteOutObjectHelper oxygenateRouteOutCodeHelper;

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

	Long smsRouteId;

	String reference;
	String status;

	Boolean success = false;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull TaskLogger taskLogger,
			@NonNull FormatWriter debugWriter) {

		smsRouteId =
			parseIntegerRequired (
				requestContext.requestStringRequired (
					"smsRouteId"));

		reference =
			requestContext.parameterRequired (
				"Reference");

		status =
			requestContext.parameterRequired (
				"Status");

	}

	@Override
	protected
	void updateDatabase (
			@NonNull TaskLogger taskLogger) {

		try (

			Transaction transaction =
				database.beginReadWrite (
					stringFormat (
						"%s.%s ()",
						getClass ().getSimpleName (),
						"updateDatabase"),
					this);

		) {

			OxygenateRouteOutRec routeOut =
				oxygenateRouteOutCodeHelper.findRequired (
					smsRouteId);

			if (! routeOut.getRoute ().getDeliveryReports ()) {

				throw new RuntimeException (
					stringFormat (
						"Delivery reports are not enabled for route %s.%s",
						routeOut.getRoute ().getSlice ().getCode (),
						routeOut.getRoute ().getCode ()));

			}

			OxygenateReportCodeRec reportCode =
				oxygenateReportCodeHelper.findByCodeRequired (
					routeOut.getOxygenateConfig (),
					status);

			RouteRec route =
				smsRouteHelper.findRequired (
					smsRouteId);

			reportLogic.deliveryReport (
				taskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String debugLog) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"storeLog");

		try (

			Transaction transaction =
				database.beginReadWrite (
					stringFormat (
						"%s.%s ()",
						getClass ().getSimpleName (),
						"storeLog"),
					this);

		) {

			oxygenateInboundLogHelper.insert (
				taskLogger,
				oxygenateInboundLogHelper.createInstance ()

				.setRoute (
					smsRouteHelper.findRequired (
						smsRouteId))

				.setType (
					OxygenateInboundLogType.smsDelivery)

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

}
