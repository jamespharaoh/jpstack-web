package wbs.smsapps.autoresponder.daemon;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetObjectHelper;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.list.logic.NumberListLogic;

import wbs.smsapps.autoresponder.model.AutoResponderObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRec;
import wbs.smsapps.autoresponder.model.AutoResponderRequestObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRequestRec;

import wbs.utils.email.EmailLogic;

@Accessors (fluent = true)
@PrototypeComponent ("autoResponderCommand")
public
class AutoResponderCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	AutoResponderObjectHelper autoResponderHelper;

	@SingletonDependency
	AutoResponderRequestObjectHelper autoResponderRequestHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EmailLogic emailLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	MessageSetObjectHelper messageSetHelper;

	@SingletonDependency
	NumberListLogic numberListLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"auto_responder.default"
		};

	}

	@Override
	public
	InboxAttemptRec handle (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handle");

		) {

			MessageRec receivedMessage =
				inbox.getMessage ();

			AutoResponderRec autoResponder =
				autoResponderHelper.findRequired (
					transaction,
					command.getParentId ());

			ServiceRec defaultService =
				serviceHelper.findByCodeRequired (
					transaction,
					autoResponder,
					"default");

			// create request

			AutoResponderRequestRec request =
				autoResponderRequestHelper.insert (
					transaction,
					autoResponderRequestHelper.createInstance ()

				.setAutoResponder (
					autoResponder)

				.setTimestamp (
					transaction.now ())

				.setNumber (
					receivedMessage.getNumber ())

				.setReceivedMessage (
					receivedMessage)

			);

			// send responses

			MessageSetRec messageSet =
				messageSetHelper.findByCodeRequired (
					transaction,
					autoResponder,
					"default");

			for (
				MessageSetMessageRec messageSetMessage
					: messageSet.getMessages ()
			) {

				MessageRec sentMessage =
					messageSenderProvider.get ()

					.threadId (
						receivedMessage.getThreadId ())

					.number (
						receivedMessage.getNumber ())

					.messageString (
						transaction,
						messageSetMessage.getMessage ())

					.numFrom (
						messageSetMessage.getNumber ())

					.route (
						messageSetMessage.getRoute ())

					.service (
						defaultService)

					.sendNow (
						! autoResponder.getSequenceResponses ()
						|| messageSetMessage.getIndex () == 0)

					.deliveryTypeCode (
						transaction,
						"auto_responder")

					.ref (
						request.getId ())

					.send (
						transaction);

				request.getSentMessages ().add (
					sentMessage);

			}

			// send email

			if (
				autoResponder.getEmailAddress () != null
				&& autoResponder.getEmailAddress ().length () > 0
			) {

				emailLogic.sendSystemEmail (
					ImmutableList.of (
						autoResponder.getEmailAddress ()),
					"Auto responder " + autoResponder.getDescription (),
					receivedMessage.getText ().getText ());

			}

			// add to number list

			if (
				isNotNull (
					autoResponder.getAddToNumberList ())
			) {

				numberListLogic.addDueToMessage (
					transaction,
					autoResponder.getAddToNumberList (),
					receivedMessage.getNumber (),
					receivedMessage,
					defaultService);

			}

			// process inbox

			return smsInboxLogic.inboxProcessed (
				transaction,
				inbox,
				optionalOf (
					defaultService),
				optionalAbsent (),
				command);

		}

	}

}
