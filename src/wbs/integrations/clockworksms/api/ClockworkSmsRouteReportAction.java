package wbs.integrations.clockworksms.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalRequired;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.lowercase;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.utf8ToString;

import java.io.ByteArrayInputStream;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.api.mvc.ApiLoggingAction;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.utils.etc.FormatWriter;
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

@PrototypeComponent ("clockworkSmsRouteReportAction")
public
class ClockworkSmsRouteReportAction
	extends ApiLoggingAction {

	// dependencies

	@Inject
	ClockworkSmsDeliveryStatusObjectHelper clockworkSmsDeliveryStatusHelper;

	@Inject
	ClockworkSmsDeliveryStatusDetailCodeObjectHelper
	clockworkSmsDeliveryStatusDetailCodeHelper;

	@Inject
	ClockworkSmsInboundLogObjectHelper clockworkSmsInboundLogHelper;

	@Inject
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper rootHelper;

	@Inject
	SmsDeliveryReportLogic smsDeliveryReportLogic;

	@Inject
	MessageObjectHelper smsMessageHelper;

	@Inject
	SmsMessageLogic smsMessageLogic;

	@Inject
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponderProvider;

	// state

	ClockworkSmsRouteReportRequest request;
	ClockworkSmsRouteReportResponse response;

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
			new DataFromXml ()

			.registerBuilderClasses (
				ClockworkSmsRouteReportRequest.class,
				ClockworkSmsRouteReportRequest.Item.class);

		request =
			(ClockworkSmsRouteReportRequest)
			dataFromXml.readInputStream (
				new ByteArrayInputStream (
					requestBytes),
				"clockwork-sms-route-report.xml",
				ImmutableList.of ());

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

		Optional<RouteRec> smsRouteOptional =
			smsRouteHelper.find (
				Long.parseLong (
					requestContext.requestStringRequired (
						"smsRouteId")));

		if (

			isNotPresent (
				smsRouteOptional)

			|| equal (
				smsRouteOptional.get ().getDeleted (),
				true)

			|| equal (
				smsRouteOptional.get ().getCanSend (),
				false)

			|| equal (
				smsRouteOptional.get ().getDeliveryReports (),
				false)

		) {
			throw new PageNotFoundException ();
		}

		RouteRec smsRoute =
			optionalRequired (
				smsRouteOptional);

		// lookup clockwork sms route in

		Optional<ClockworkSmsRouteOutRec> clockworkSmsRouteOutOptional =
			clockworkSmsRouteOutHelper.find (
				smsRoute.getId ());

		if (

			isNotPresent (
				clockworkSmsRouteOutOptional)

			|| equal (
				clockworkSmsRouteOutOptional.get ().getDeleted (),
				true)

		) {
			throw new PageNotFoundException ();
		}

		ClockworkSmsRouteOutRec clockworkSmsRouteOut =
			optionalRequired (
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

	}

	private
	ClockworkSmsRouteReportResponse.Item handleDeliveryReport (
			@NonNull ClockworkSmsRouteOutRec clockworkSmsRouteOut,
			@NonNull ClockworkSmsRouteReportRequest.Item item) {

		// lookup the delivery status

		Optional<ClockworkSmsDeliveryStatusRec> deliveryStatusOptional =
			clockworkSmsDeliveryStatusHelper.findByCode (
				clockworkSmsRouteOut.getClockworkSmsConfig (),
				lowercase (
					item.status ()));

		if (
			isNotPresent (
				deliveryStatusOptional)
		) {

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"status not recognised");

		}

		ClockworkSmsDeliveryStatusRec deliveryStatus =
			optionalRequired (
				deliveryStatusOptional);

		// lookup the delivery status detail code

		Optional<ClockworkSmsDeliveryStatusDetailCodeRec>
		deliveryStatusDetailCodeOptional =
			clockworkSmsDeliveryStatusDetailCodeHelper.findByCode (
				clockworkSmsRouteOut.getClockworkSmsConfig (),
				item.errCode ());

		if (
			isNotPresent (
				deliveryStatusDetailCodeOptional)
		) {

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"status detail code not recognised");

		}

		ClockworkSmsDeliveryStatusDetailCodeRec deliveryStatusDeliveryCode =
			optionalRequired (
				deliveryStatusDetailCodeOptional);

		// lookup the message

		Optional<MessageRec> smsMessageOptional =
			smsMessageLogic.findMessageByMangledId (
				item.clientId ());

		if (
			isNotPresent (
				smsMessageOptional)
		) {

			return new ClockworkSmsRouteReportResponse.Item ()

				.dlrId (
					item.dlrId ())

				.response (
					"message not recognised");

		}

		MessageRec smsMessage =
			optionalRequired (
				smsMessageOptional);

		// store the delivery report

		try {

			smsDeliveryReportLogic.deliveryReport (
				smsMessage,
				deliveryStatus.getMessageStatus (),
				Optional.of (
					item.status ()),
				Optional.of (
					ifElse (
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

		);

		transaction.commit ();

	}

}
