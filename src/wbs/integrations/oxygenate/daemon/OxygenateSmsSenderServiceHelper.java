package wbs.integrations.oxygenate.daemon;

import static wbs.utils.etc.LogicUtils.booleanToZeroOne;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitComma;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.oxygenate.foreignapi.OxygenateSmsSendRequest;
import wbs.integrations.oxygenate.foreignapi.OxygenateSmsSendResponse;
import wbs.integrations.oxygenate.foreignapi.OxygenateSmsSender;
import wbs.integrations.oxygenate.model.OxygenateNetworkObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateNetworkRec;
import wbs.integrations.oxygenate.model.OxygenateRouteOutObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteOutRec;

import wbs.sms.gsm.GsmUtils;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("oxygenateSmsSenderServiceHelper")
public
class OxygenateSmsSenderServiceHelper
	implements SmsSenderHelper <OxygenateSmsSender> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	OxygenateNetworkObjectHelper oxygenateNetworkHelper;

	@SingletonDependency
	OxygenateRouteOutObjectHelper oxygenateRouteOutHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <OxygenateSmsSender> oxygenateSmsSenderProvider;

	// details

	@Override
	public
	String senderCode () {
		return "oxygen8";
	}

	// implementation

	@Override
	public
	SetupRequestResult <OxygenateSmsSender> setupRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OutboxRec smsOutbox) {

		// get stuff

		MessageRec smsMessage =
			smsOutbox.getMessage ();

		RouteRec smsRoute =
			smsMessage.getRoute ();

		// lookup route out

		Optional <OxygenateRouteOutRec> oxygenateRouteOutOptional =
			oxygenateRouteOutHelper.find (
				smsRoute.getId ());

		if (
			optionalIsNotPresent (
				oxygenateRouteOutOptional)
		) {

			return new SetupRequestResult <OxygenateSmsSender> ()

				.status (
					SetupRequestStatus.configError)

				.statusMessageFormat (
					"Oxygen8 outbound route not found for %s",
					smsRoute.getCode ());

		}

		OxygenateRouteOutRec oxygenateRouteOut =
			oxygenateRouteOutOptional.get ();

		// lookup network

		Optional <OxygenateNetworkRec> oxygenateNetworkOptional =
			oxygenateNetworkHelper.find (
				oxygenateRouteOut.getOxygenateConfig (),
				smsMessage.getNumber ().getNetwork ());

		if (
			optionalIsNotPresent (
				oxygenateNetworkOptional)
		) {

			return new SetupRequestResult <OxygenateSmsSender> ()

				.status (
					SetupRequestStatus.configError)

				.statusMessageFormat (
					"Oxygen8 network not found for %s",
					smsMessage.getNumber ().getNetwork ().getCode ());

		}

		OxygenateNetworkRec oxygenateNetwork =
			oxygenateNetworkOptional.get ();

		// validate message text

		if (
			! GsmUtils.gsmStringIsValid (
				smsMessage.getText ().getText ())
		) {

			return new SetupRequestResult <OxygenateSmsSender> ()

				.status (
					SetupRequestStatus.validationError)

				.statusMessage (
					"The message text contains non-GSM characters");

		}

		long gsmLength =
			GsmUtils.gsmStringLength (
				smsMessage.getText ().getText ());

		boolean needMultipart =
			gsmLength > 160;

		boolean allowMultipart =
			oxygenateRouteOut.getMultipart ();

		if (
			needMultipart
			&& ! allowMultipart
		) {

			return new SetupRequestResult <OxygenateSmsSender> ()

				.status (
					SetupRequestStatus.validationError)

				.statusMessageFormat (
					"Length is %s but multipart not enabled",
					integerToDecimalString (
						gsmLength));

		}

		// create request

		OxygenateSmsSendRequest request =
			new OxygenateSmsSendRequest ()

			.relayUrl (
				oxygenateRouteOut.getRelayUrl ())

			.reference (
				integerToDecimalString (
					smsMessage.getId ()))

			.campaignId (
				oxygenateRouteOut.getCampaignId ())

			.username (
				oxygenateRouteOut.getUsername ())

			.password (
				oxygenateRouteOut.getPassword ())

			.multipart (
				booleanToZeroOne (
					oxygenateRouteOut.getMultipart ()))

			.shortcode (
				ifThenElse (
					oxygenateRouteOut.getPremium (),
					() -> oxygenateRouteOut.getShortcode (),
					() -> null))

			.mask (
				ifThenElse (
					! oxygenateRouteOut.getPremium (),
					() -> smsMessage.getNumFrom (),
					() -> null))

			.channel (
				ifThenElse (
					oxygenateRouteOut.getPremium (),
					() -> oxygenateNetwork.getChannel (),
					() -> "BULK"))

			.msisdn (
				smsMessage.getNumTo ())

			.content (
				smsMessage.getText ().getText ())

			.premium (
				booleanToZeroOne (
					smsRoute.getOutCharge () > 0))

		;

		// create sender

		OxygenateSmsSender sender =
			oxygenateSmsSenderProvider.get ()

			.request (
				request)

		;

		// encode request

		sender.encode ();

		// return

		return new SetupRequestResult <OxygenateSmsSender> ()

			.status (
				SetupRequestStatus.success)

			.requestTrace (
				sender.requestTrace ())

			.state (
				sender);

	}

	@Override
	public
	PerformSendResult performSend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OxygenateSmsSender sender) {
	
		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"performSend");

		PerformSendResult result =
			new PerformSendResult ();

		// encode

		try {

			sender.send ();

			sender.receive ();

			result.responseTrace (
				sender.responseTrace ());

			sender.decode (
				taskLogger);

			return result

				.status (
					PerformSendStatus.success);

		} catch (Exception exception) {

			return result

				.status (
					PerformSendStatus.communicationError)

				.exception (
					exception);

		}

	}

	@Override
	public
	ProcessResponseResult processSend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OxygenateSmsSender sender) {

		OxygenateSmsSendResponse response =
			sender.response ();

		if (
			stringNotEqualSafe (
				response.statusCode (),
				"101")
		) {

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.remoteError)

				.statusMessageFormat (
					"Unrecognised response code: %s",
					response.statusCode ());

		}

		return new ProcessResponseResult ()

			.status (
				ProcessResponseStatus.success)

			.otherIds (
				stringSplitComma (
					response.messageReferences ()));

	}

}
