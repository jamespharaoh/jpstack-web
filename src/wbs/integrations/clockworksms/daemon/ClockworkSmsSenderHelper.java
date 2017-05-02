package wbs.integrations.clockworksms.daemon;

import static wbs.sms.gsm.GsmUtils.gsmCountMessageParts;
import static wbs.sms.gsm.GsmUtils.gsmStringIsNotValid;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.clockworksms.foreignapi.ClockworkSmsMessageRequest;
import wbs.integrations.clockworksms.foreignapi.ClockworkSmsMessageResponse;
import wbs.integrations.clockworksms.foreignapi.ClockworkSmsMessageSender;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutRec;

import wbs.platform.scaffold.model.RootObjectHelper;

import wbs.sms.gsm.GsmUtils;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper;
import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("clockworkSmsSenderHelper")
public
class ClockworkSmsSenderHelper
	implements SmsSenderHelper <ClockworkSmsMessageSender> {

	// singleton dependencies

	@SingletonDependency
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	SmsMessageLogic smsMessageLogic;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <ClockworkSmsMessageSender> clockworkSmsMessageSenderProvider;

	// details

	@Override
	public
	String senderCode () {
		return "clockwork_sms";
	}

	// implementation

	@Override
	public
	SetupRequestResult <ClockworkSmsMessageSender> setupRequest (
			@NonNull Transaction parentTransaction,
			@NonNull OutboxRec smsOutbox) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setupRequest");

		) {

			// get stuff

			MessageRec smsMessage =
				smsOutbox.getMessage ();

			RouteRec smsRoute =
				smsOutbox.getRoute ();

			// lookup route out

			Optional <ClockworkSmsRouteOutRec> clockworkSmsRouteOutOptional =
				clockworkSmsRouteOutHelper.find (
					transaction,
					smsRoute.getId ());

			if (
				optionalIsNotPresent (
					clockworkSmsRouteOutOptional)
			) {

				return new SetupRequestResult <ClockworkSmsMessageSender> ()

					.status (
						SetupRequestStatus.configError)

					.statusMessage (
						stringFormat (
							"Clockwork SMS outbound route not found for %s",
							smsRoute.getCode ()));

			}

			ClockworkSmsRouteOutRec clockworkSmsRouteOut =
				clockworkSmsRouteOutOptional.get ();

			// validate message text

			if (
				gsmStringIsNotValid (
					smsMessage.getText ().getText ())
			) {

				return new SetupRequestResult <ClockworkSmsMessageSender> ()

					.status (
						SetupRequestStatus.validationError)

					.statusMessage (
						"The message text contains non-GSM characters");

			}

			long gsmLength =
				GsmUtils.gsmStringLength (
					smsMessage.getText ().getText ());

			long gsmParts =
				GsmUtils.gsmCountMessageParts (
					gsmLength);

			if (
				lessThan (
					clockworkSmsRouteOut.getMaxParts (),
					gsmParts)
			) {

				return new SetupRequestResult <ClockworkSmsMessageSender> ()

					.status (
						SetupRequestStatus.validationError)

					.statusMessage (
						stringFormat (
							"Message has length %s ",
							integerToDecimalString (
								gsmLength),
							"and so would be split into %s parts ",
							integerToDecimalString (
								gsmParts),
							"but the maximum configured for this route is %s",
							integerToDecimalString (
								clockworkSmsRouteOut.getMaxParts ())));

			}

			// pick a handler

			if (
				stringEqualSafe (
					smsMessage.getMessageType ().getCode (),
					"sms")
			) {

				// nothing to do

			} else {

				return new SetupRequestResult <ClockworkSmsMessageSender> ()

					.status (
						SetupRequestStatus.unknownError)

					.statusMessage (
						stringFormat (
							"Don't know what to do with a %s",
							smsMessage.getMessageType ().getCode ()));

			}

			// create request

			ClockworkSmsMessageRequest clockworkRequest =
				new ClockworkSmsMessageRequest ()

				.key (
					clockworkSmsRouteOut.getKey ())

				.sms (
					ImmutableList.of (
						new ClockworkSmsMessageRequest.Sms ()

					.to (
						smsMessage.getNumTo ())

					.from (
						smsMessage.getNumFrom ())

					.content (
						smsMessage.getText ().getText ())

					.msgType (
						"TEXT")

					.concat (
						clockworkSmsRouteOut.getMaxParts ())

					.clientId (
						smsMessageLogic.mangleMessageId (
							transaction,
							smsMessage.getId ()))

					.dlrType (
						4l)

					.dlrUrl (
						stringFormat (
							"%s/clockwork-sms/route/%s/report",
							wbsConfig.apiUrl (),
							integerToDecimalString (
								smsRoute.getId ())))

					.uniqueId (
						1l)

					.invalidCharAction (
						1l)

					.truncate (
						0l)

				));

			// create sender

			ClockworkSmsMessageSender clockworkSender =
				clockworkSmsMessageSenderProvider.get ()

				.url (
					clockworkSmsRouteOut.getUrl ())

				.simulateMultipart (
					clockworkSmsRouteOut.getSimulateMultipart ())

				.request (
					clockworkRequest);

			// encode request

			clockworkSender.encode ();

			// return

			return new SetupRequestResult <ClockworkSmsMessageSender> ()

				.status (
					SetupRequestStatus.success)

				.requestTrace (
					clockworkSender.requestTrace ())

				.state (
					clockworkSender);

		}

	}

	@Override
	public
	PerformSendResult performSend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ClockworkSmsMessageSender clockworkSender) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"performSend");

		) {

			PerformSendResult result =
				new PerformSendResult ();

			// encode

			try {

				clockworkSender.send ();

				clockworkSender.receive ();

				result.responseTrace (
					clockworkSender.responseTrace ());

				clockworkSender.decode (
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

	}

	@Override
	public
	ProcessResponseResult processSend (
			@NonNull Transaction parentTransaction,
			@NonNull ClockworkSmsMessageSender clockworkSender) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processSend");

		) {

			ClockworkSmsMessageResponse clockworkResponse =
				clockworkSender.clockworkResponse ();

			// check for general error

			if (
				isNotNull (
					clockworkResponse.errNo ())
			) {

				return handleGeneralError (
					clockworkResponse);

			}

			// check for individual error

			ClockworkSmsMessageResponse.SmsResp clockworkSmsResponse =
				clockworkResponse.smsResp ().get (0);

			if (
				isNotNull (
					clockworkSmsResponse.errNo ())
			) {

				return handleSpecificError (
					clockworkSmsResponse);

			}

			// success response

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.success)

				.otherIds (
					ImmutableList.of (
						clockworkSmsResponse.messageId ()))

				.simulateMessageParts (
					ifThenElse (
						clockworkSender.simulateMultipart (),
						() -> gsmCountMessageParts (
							clockworkSender.request ().sms ().get (0).content ()),
						() -> null));

		}

	}

	ProcessResponseResult handleGeneralError (
			@NonNull ClockworkSmsMessageResponse clockworkResponse) {

		return new ProcessResponseResult ()

			.status (
				ProcessResponseStatus.remoteError)

			.statusMessage (
				clockworkResponse.errDesc ())

			.failureType (
				FailureType.temporary);

	}

	ProcessResponseResult handleSpecificError (
			@NonNull ClockworkSmsMessageResponse.SmsResp clockworkSmsResponse) {

		switch (clockworkSmsResponse.errNo ()) {

		case 25: // client id duplicated

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.success)

				.statusMessage (
					"Duplicate client ID, message already sent");

		case 60: // blocked by spam filter

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.remoteError)

				.statusMessage (
					"Blocked by Clockwork SMS's spam filter")

				.failureType (
					FailureType.permanent);

		default:

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.remoteError)

				.statusMessage (
					stringFormat (
						"Server error %s: %s",
						integerToDecimalString (
							clockworkSmsResponse.errNo ()),
						clockworkSmsResponse.errDesc ()))

				.failureType (
					FailureType.temporary);

		}

	}

}
