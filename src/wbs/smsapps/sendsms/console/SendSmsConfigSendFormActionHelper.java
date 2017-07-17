package wbs.smsapps.sendsms.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.text.console.TextConsoleHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.sendsms.model.SendSmsConfigRec;

import wbs.web.responder.WebResponder;

@SingletonComponent ("sendSmsConfigSendFormActionHelper")
public
class SendSmsConfigSendFormActionHelper
	implements ConsoleFormActionHelper <
		SendSmsConfigSendForm,
		Object
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	NumberConsoleHelper smsNumberHelper;

	@SingletonDependency
	SendSmsConfigConsoleHelper sendSmsConfigHelper;

	@SingletonDependency
	SendSmsMessageConsoleHelper sendSmsMessageHelper;

	@SingletonDependency
	TextConsoleHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SmsMessageSender> smsMessageSenderProvider;

	// implementation

	@Override
	public
	void updatePassiveFormState (
			@NonNull Transaction parentTransaction,
			@NonNull SendSmsConfigSendForm form) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updatePassiveFormState");

		) {

			SendSmsConfigRec sendSmsConfig =
				sendSmsConfigHelper.findFromContextRequired (
					transaction);

			form

				.originator (
					sendSmsConfig.getOriginator ());

		}

	}

	@Override
	public
	Optional <WebResponder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull SendSmsConfigSendForm form) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			// send message

			SendSmsConfigRec sendSmsConfig =
				sendSmsConfigHelper.findFromContextRequired (
					transaction);

			NumberRec smsNumber =
				smsNumberHelper.findOrCreate (
					transaction,
					form.number ());

			TextRec messageBodyText =
				textHelper.findOrCreate (
					transaction,
					form.messageBody ());

			MessageRec smsMessage =
				smsMessageSenderProvider.provide (
					transaction)

				.routerResolve (
					transaction,
					sendSmsConfig.getSmsRouter ())

				.serviceLookup (
					transaction,
					sendSmsConfig,
					"default")

				.numFrom (
					sendSmsConfig.getOriginator ())

				.number (
					smsNumber)

				.messageText (
					messageBodyText)

				.user (
					userConsoleLogic.userRequired (
						transaction))

				.send (
					transaction);

			sendSmsMessageHelper.insert (
				transaction,
				sendSmsMessageHelper.createInstance ()

				.setSendSmsConfig (
					sendSmsConfig)

				.setTimestamp (
					transaction.now ())

				.setNumber (
					smsNumber)

				.setOriginator (
					form.originator ())

				.setMessageBody (
					messageBodyText)

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

				.setMessage (
					smsMessage)

			);

			transaction.commit ();

			requestContext.addNotice (
				"Message sent");

			requestContext.setEmptyFormData ();

			return optionalAbsent ();

		}

	}

}
