package wbs.apn.chat.contact.console;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.exception.logic.ExceptionLogLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.text.console.TextConsoleHelper;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.gsm.GsmUtils;

import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatMessagePendingFormAction")
public
class ChatMessagePendingFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatContactConsoleHelper chatContactHelper;

	@SingletonDependency
	ChatHelpLogic chatHelpLogic;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatMiscLogic chatLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ExceptionLogLogic exceptionLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	TextConsoleHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatMessagePendingFormResponder");
	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// get the message id

		Long chatMessageId =
			requestContext.parameterIntegerRequired (
				"chat_message_id");

		requestContext.request (
			"chatMessageId",
			chatMessageId);

		// delegate appropriately

		if (

			optionalIsPresent (
				requestContext.parameter (
					"send"))

			|| optionalIsPresent (
				requestContext.parameter (
					"sendWithoutApproval"))
		) {
			return goSend ();
		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"reject"))
		) {
			return goReject ();
		}

		throw new RuntimeException (
			"Expected send or reject parameters");

	}

	private
	Responder goSend () {

		// get params

		String messageParam =
			requestContext.parameterRequired (
				"message");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatMessagePendingFormAction.goSend ()",
					this);

		) {

			// get database objects

			ChatMessageRec chatMessage =
				chatMessageHelper.findFromContextRequired ();

			ChatRec chat =
				chatMessage.getChat ();

			// check message is ok

			if (
				optionalIsNotPresent (
					requestContext.parameter (
						"sendWithoutApproval"))
			) {

				ChatMessageLogic.ApprovalResult approvalResult =
					chatMessageLogic.checkForApproval (
						chat,
						messageParam);

				if (approvalResult.status
						!= ChatMessageLogic.ApprovalResult.Status.clean) {

					requestContext.addWarning (
						"Message still contains questionable content, use " +
						"the 'no warning' button to send anyway");

					requestContext.request (
						"showSendWithoutApproval",
						true);

					return null;

				}

			}

			// confirm message status

			if (chatMessage.getStatus ()
					!= ChatMessageStatus.moderatorPending) {

				requestContext.addError (
					"Message is already approved");

				return responder ("queueHomeResponder");

			}

			// process the queue item

			queueLogic.processQueueItem (
				chatMessage.getQueueItem (),
				userConsoleLogic.userRequired ());

			// update the chat message

			chatMessage

				.setModerator (
					userConsoleLogic.userRequired ())

				.setModeratorTimestamp (
					transaction.now ());

			if (
				stringEqualSafe (
					messageParam,
					chatMessage.getOriginalText ().getText ())
			) {

				// original message was approved

				chatMessage

					.setStatus (
						ChatMessageStatus.moderatorApproved)

					.setEditedText (
						chatMessage.getOriginalText ());

			} else if (
				stringEqualSafe (
					messageParam,
					chatMessage.getEditedText ().getText ())
			) {

				// automatically edited message was accepted

				chatMessage

					.setStatus (
						ChatMessageStatus.moderatorAutoEdited);

				chatMessageLogic.chatUserRejectionCountInc (
					chatMessage.getFromUser (),
					chatMessage.getThreadId ());

				chatMessageLogic.chatUserRejectionCountInc (
					chatMessage.getToUser (),
					chatMessage.getThreadId ());

			} else {

				// moderator made changes to message

				chatMessage
					.setStatus (ChatMessageStatus.moderatorEdited)
					.setEditedText (
						textHelper.findOrCreate (messageParam));

				chatMessageLogic.chatUserRejectionCountInc (
					chatMessage.getFromUser (),
					chatMessage.getThreadId ());

				chatMessageLogic.chatUserRejectionCountInc (
					chatMessage.getToUser (),
					chatMessage.getThreadId ());

			}

			// update chat user contact

			ChatContactRec chatContact =
				chatContactHelper.findOrCreate (
					chatMessage.getFromUser (),
					chatMessage.getToUser ());

			chatContact

				.setLastDeliveredMessageTime (
					transaction.now ());

			// and send it

			switch (chatMessage.getToUser ().getType ()) {

			case user:

				chatMessageLogic.chatMessageDeliverToUser (
					chatMessage);

				break;

			case monitor:

				ChatMonitorInboxRec chatMonitorInbox =
					chatMessageLogic.findOrCreateChatMonitorInbox (
						chatMessage.getToUser (),
						chatMessage.getFromUser (),
						false);

				chatMonitorInbox.setInbound (true);

				break;

			default:

				throw new RuntimeException ("Not a user or monitor");

			}

			transaction.commit ();

			// we're done

			requestContext.addNotice (
				"Message approved");

			return responder (
				"queueHomeResponder");

		}

	}

	private
	Responder goReject () {

		// get params

		String messageParam =
			stringTrim (
				requestContext.parameterRequired (
					"message"));

		if (GsmUtils.gsmStringLength (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (GsmUtils.gsmStringLength (messageParam) > 149) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatMessagePendingFormAction.goReject ()",
				this);

		// get database objects

		ChatMessageRec chatMessage =
			chatMessageHelper.findFromContextRequired ();

		// confirm message status

		if (chatMessage.getStatus () != ChatMessageStatus.moderatorPending) {

			requestContext.addError (
				"Message is already approved");

			return responder ("queueHomeResponder");

		}

		// remove the queue item

		queueLogic.processQueueItem (
			chatMessage.getQueueItem (),
			userConsoleLogic.userRequired ());

		// update the chatMessage

		chatMessage

			.setModerator (
				userConsoleLogic.userRequired ())

			.setStatus (
				ChatMessageStatus.moderatorRejected)

			.setEditedText (
				null);

		// and send help message

		chatHelpLogic.sendHelpMessage (
			userConsoleLogic.userRequired (),
			chatMessage.getFromUser (),
			messageParam,
			Optional.of (
				chatMessage.getThreadId ()),
			Optional.<ChatHelpLogRec>absent ());

		// inc rejection count

		chatMessageLogic.chatUserRejectionCountInc (
			chatMessage.getFromUser (),
			chatMessage.getThreadId ());

		transaction.commit ();

		requestContext.addNotice (
			"Rejection sent");

		return responder (
			"queueHomeResponder");

	}

}
