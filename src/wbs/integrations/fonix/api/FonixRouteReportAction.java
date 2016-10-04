package wbs.integrations.fonix.api;

import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.utf8ToString;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromGeneric;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.fonix.logic.FonixLogic;
import wbs.integrations.fonix.model.FonixDeliveryStatusObjectHelper;
import wbs.integrations.fonix.model.FonixDeliveryStatusRec;
import wbs.integrations.fonix.model.FonixInboundLogObjectHelper;
import wbs.integrations.fonix.model.FonixInboundLogType;
import wbs.integrations.fonix.model.FonixRouteOutObjectHelper;
import wbs.integrations.fonix.model.FonixRouteOutRec;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.utils.string.FormatWriter;

@PrototypeComponent ("fonixRouteReportAction")
public
class FonixRouteReportAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	FonixDeliveryStatusObjectHelper fonixDeliveryStatusHelper;

	@SingletonDependency
	FonixInboundLogObjectHelper fonixInboundLogHelper;

	@SingletonDependency
	FonixRouteOutObjectHelper fonixRouteOutHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	FonixLogic fonixLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	SmsDeliveryReportLogic smsDeliveryReportLogic;

	@SingletonDependency
	MessageObjectHelper smsMessageHelper;

	@SingletonDependency
	SmsMessageLogic smsMessageLogic;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	FonixRouteReportRequest request;
	Boolean success = false;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull FormatWriter debugWriter) {

		// read and log request

		byte[] requestBytes =
			requestContext.requestBodyRaw ();

		debugWriter.writeString (
			"== REQUEST BODY ==\n\n");

		debugWriter.writeString (
			utf8ToString (
				requestBytes));

		debugWriter.writeString (
			"\n\n");

		// decode request

		request =
			new DataFromGeneric ()

			.fromMap (
				FonixRouteReportRequest.class,
				requestContext.parameterMapSimple ());

	}

	@Override
	protected
	void updateDatabase () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s ()",
					getClass ().getSimpleName (),
					"updateDatabase"),
				this);

		// lookup route

		Optional <RouteRec> smsRouteOptional =
			smsRouteHelper.find (
				Long.parseLong (
					requestContext.requestStringRequired (
						"smsRouteId")));

		if (

			optionalIsNotPresent (
				smsRouteOptional)

			|| booleanEqual (
				smsRouteOptional.get ().getDeleted (),
				true)

			|| booleanEqual (
				smsRouteOptional.get ().getCanSend (),
				false)

			|| booleanEqual (
				smsRouteOptional.get ().getDeliveryReports (),
				false)

		) {
			throw new PageNotFoundException ();
		}

		RouteRec smsRoute =
			optionalGetRequired (
				smsRouteOptional);

		// lookup fonix route in

		Optional <FonixRouteOutRec> fonixRouteOutOptional =
			fonixRouteOutHelper.find (
				smsRoute.getId ());

		if (

			optionalIsNotPresent (
				fonixRouteOutOptional)

			|| booleanEqual (
				fonixRouteOutOptional.get ().getDeleted (),
				true)

		) {
			throw new PageNotFoundException ();
		}

		FonixRouteOutRec fonixRouteOut =
			optionalGetRequired (
				fonixRouteOutOptional);

		// process delivery report

		handleDeliveryReport (
			fonixRouteOut);

		// commit and return

		transaction.commit ();

	}

	private
	void handleDeliveryReport (
			@NonNull FonixRouteOutRec fonixRouteOut) {

		// lookup the delivery status

		Optional <FonixDeliveryStatusRec> deliveryStatusOptional =
			fonixDeliveryStatusHelper.findByCode (
				fonixRouteOut.getFonixConfig (),
				lowercase (
					request.statusCode ()));

		if (
			optionalIsNotPresent (
				deliveryStatusOptional)
		) {

			exceptionLogger.logSimple (
				"webapi",
				requestContext.requestUri (),
				stringFormat (
					"Delivery status not recognised: %s",
					request.statusCode ()),
				"",
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

			return;

		}

		FonixDeliveryStatusRec deliveryStatus =
			optionalGetRequired (
				deliveryStatusOptional);

		// lookup the message

		Optional <MessageRec> smsMessageOptional =
			smsMessageLogic.findMessageByMangledId (
				request.guid ());

		if (
			optionalIsNotPresent (
				smsMessageOptional)
		) {

			exceptionLogger.logSimple (
				"webapi",
				requestContext.requestUri (),
				stringFormat (
					"Message guid not recognised: %s",
					request.guid ()),
				"",
				optionalAbsent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

			return;

		}

		MessageRec smsMessage =
			optionalGetRequired (
				smsMessageOptional);

		// store the delivery report

		try {

			smsDeliveryReportLogic.deliveryReport (
				smsMessage,
				deliveryStatus.getMessageStatus (),
				optionalOf (
					request.statusCode ()),
				optionalOf  (
					request.statusText ()),
				optionalAbsent (),
				optionalOf (
					fonixLogic.stringToInstant (
						request.statusTime ())));

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"webapi",
				requestContext.requestUri (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

		}

	}

	@Override
	protected
	Responder createResponse (
			@NonNull FormatWriter debugWriter) {

		// encode response

		String responseString = "OK";

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
			@NonNull String debugLog) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s ()",
					getClass ().getSimpleName (),
					"storeLog"),
				this);

		fonixInboundLogHelper.insert (
			fonixInboundLogHelper.createInstance ()

			.setRoute (
				smsRouteHelper.findRequired (
					Long.parseLong (
						requestContext.requestStringRequired (
							"smsRouteId"))))

			.setType (
				FonixInboundLogType.smsDelivery)

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
