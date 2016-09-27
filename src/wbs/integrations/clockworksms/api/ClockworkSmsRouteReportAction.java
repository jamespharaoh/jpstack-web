package wbs.integrations.clockworksms.api;

import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.ByteArrayInputStream;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.clockworksms.model.ClockworkSmsDeliveryStatusDetailCodeObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsDeliveryStatusDetailCodeRec;
import wbs.integrations.clockworksms.model.ClockworkSmsDeliveryStatusObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsDeliveryStatusRec;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogType;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutRec;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.utils.string.FormatWriter;

@PrototypeComponent ("clockworkSmsRouteReportAction")
public
class ClockworkSmsRouteReportAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	ClockworkSmsDeliveryStatusObjectHelper clockworkSmsDeliveryStatusHelper;

	@SingletonDependency
	ClockworkSmsDeliveryStatusDetailCodeObjectHelper
	clockworkSmsDeliveryStatusDetailCodeHelper;

	@SingletonDependency
	ClockworkSmsInboundLogObjectHelper clockworkSmsInboundLogHelper;

	@SingletonDependency
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper rootHelper;

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

	ClockworkSmsRouteReportRequest request;
	ClockworkSmsRouteReportResponse response;
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

		DataFromXml dataFromXml =
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				ClockworkSmsRouteReportRequest.class,
				ClockworkSmsRouteReportRequest.Item.class)

			.build ();

		request =
			(ClockworkSmsRouteReportRequest)
			dataFromXml.readInputStream (
				new ByteArrayInputStream (
					requestBytes),
				"clockwork-sms-route-report.xml");

	}

	@Override
	protected
	void updateDatabase () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ClockworkSmsRouteOutAction.handle ()",
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

		// lookup clockwork sms route in

		Optional <ClockworkSmsRouteOutRec> clockworkSmsRouteOutOptional =
			clockworkSmsRouteOutHelper.find (
				smsRoute.getId ());

		if (

			optionalIsNotPresent (
				clockworkSmsRouteOutOptional)

			|| booleanEqual (
				clockworkSmsRouteOutOptional.get ().getDeleted (),
				true)

		) {
			throw new PageNotFoundException ();
		}

		ClockworkSmsRouteOutRec clockworkSmsRouteOut =
			optionalGetRequired (
				clockworkSmsRouteOutOptional);

		// iterate delivery reports

		response =
			new ClockworkSmsRouteReportResponse ();

		for (
			ClockworkSmsRouteReportRequest.Item item
				: request.items ()
		) {

			response.items.add (
				handleDeliveryReport (
					clockworkSmsRouteOut,
					item));

		}

		// commit and return

		transaction.commit ();

		success = true;

	}

	private
	ClockworkSmsRouteReportResponse.Item handleDeliveryReport (
			@NonNull ClockworkSmsRouteOutRec clockworkSmsRouteOut,
			@NonNull ClockworkSmsRouteReportRequest.Item item) {

		// lookup the delivery status

		Optional <ClockworkSmsDeliveryStatusRec> deliveryStatusOptional =
			clockworkSmsDeliveryStatusHelper.findByCode (
				clockworkSmsRouteOut.getClockworkSmsConfig (),
				lowercase (
					item.status ()));

		if (
			optionalIsNotPresent (
				deliveryStatusOptional)
		) {

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"status not recognised");

		}

		ClockworkSmsDeliveryStatusRec deliveryStatus =
			optionalGetRequired (
				deliveryStatusOptional);

		// lookup the delivery status detail code

		Optional <ClockworkSmsDeliveryStatusDetailCodeRec>
		deliveryStatusDetailCodeOptional =
			clockworkSmsDeliveryStatusDetailCodeHelper.findByCode (
				clockworkSmsRouteOut.getClockworkSmsConfig (),
				item.errCode ());

		if (
			optionalIsNotPresent (
				deliveryStatusDetailCodeOptional)
		) {

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"status detail code not recognised");

		}

		ClockworkSmsDeliveryStatusDetailCodeRec deliveryStatusDeliveryCode =
			optionalGetRequired (
				deliveryStatusDetailCodeOptional);

		// lookup the message

		Optional <MessageRec> smsMessageOptional =
			smsMessageLogic.findMessageByMangledId (
				item.clientId ());

		if (
			optionalIsNotPresent (
				smsMessageOptional)
		) {

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"message not recognised");

		}

		MessageRec smsMessage =
			optionalGetRequired (
				smsMessageOptional);

		// store the delivery report

		try {

			smsDeliveryReportLogic.deliveryReport (
				smsMessage,
				deliveryStatus.getMessageStatus (),
				Optional.of (
					item.status ()),
				Optional.of (
					ifThenElse (
						isNotNull (
							deliveryStatusDeliveryCode),
						() -> stringFormat (
							"%s — %s",
							deliveryStatus.getTheirDescription (),
							deliveryStatusDeliveryCode.getTheirDescription ()),
						() -> deliveryStatus.getTheirDescription ())),
				Optional.of (
					joinWithSpace (
						stringFormat (
							"status=%s",
							item.status ()),
						stringFormat (
							"errCode=%s",
							item.errCode ()))),
				Optional.absent ());

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"ok");

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"webapi",
				requestContext.requestUri (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.ignoreWithThirdPartyWarning);

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"internal error");

		}

	}

	@Override
	protected
	Responder createResponse (
			@NonNull FormatWriter debugWriter) {

		// encode response

		DataToXml dataToXml =
			new DataToXml ();

		String responseString =
			dataToXml.writeToString (
				response);

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
				"application/xml")

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
				"ClockworkSmsRouteReportAction.storeLog ()",
				this);

		clockworkSmsInboundLogHelper.insert (
			clockworkSmsInboundLogHelper.createInstance ()

			.setRoute (
				smsRouteHelper.findRequired (
					Long.parseLong (
						requestContext.requestStringRequired (
							"smsRouteId"))))

			.setType (
				ClockworkSmsInboundLogType.smsDelivery)

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
