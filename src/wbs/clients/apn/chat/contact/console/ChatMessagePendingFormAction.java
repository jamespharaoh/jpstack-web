package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Date;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogic;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.gsm.Gsm;

@PrototypeComponent ("chatMessagePendingFormAction")
public
class ChatMessagePendingFormAction
	extends ConsoleAction {

	@Inject
	ChatContactObjectHelper chatContactHelper;

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	Database database;

	@Inject
	QueueLogic queueLogic;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatMessagePendingFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		// get the message id

		int chatMessageId =
			Integer.parseInt (
				requestContext.parameter ("chat_message_id"));

		requestContext.request (
			"chatMessageId",
			chatMessageId);

		// delegate appropriately

		if (requestContext.parameter ("send") != null
				|| requestContext.parameter ("sendWithoutApproval") != null)
			return goSend ();

		if (requestContext.parameter ("reject") != null)
			return goReject ();

		throw new RuntimeException (
			"Expected send or reject parameters");

	}

	private
	Responder goSend () {

		// get params

		String messageParam =
			requestContext.parameter ("message");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// get database objects

		ChatMessageRec chatMessage =
			chatMessageHelper.find (
				requestContext.stuffInt ("chatMessageId"));

		ChatRec chat =
			chatMessage.getChat ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// check message is ok

		if (requestContext.parameter ("sendWithoutApproval") == null) {

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
			myUser);

		// update the chat message

		chatMessage

			.setModerator (
				myUser)

			.setModeratorTimestamp (
				instantToDate (
					transaction.now ()));

		if (
			equal (
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
			equal (
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
			.setLastDeliveredMessageTime (new Date ());

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

		requestContext.addNotice ("Message approved");

		return responder ("queueHomeResponder");

	}

	private
	Responder goReject () {

		// get params

		String messageParam =
			requestContext.parameter ("message")
				.trim ();

		if (Gsm.length(messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (Gsm.length (messageParam) > 149) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// get database objects

		ChatMessageRec chatMessage =
			chatMessageHelper.find (
				requestContext.stuffInt ("chatMessageId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// confirm message status

		if (chatMessage.getStatus () != ChatMessageStatus.moderatorPending) {

			requestContext.addError (
				"Message is already approved");

			return responder ("queueHomeResponder");

		}

		// remove the queue item

		queueLogic.processQueueItem (
			chatMessage.getQueueItem (),
			myUser);

		// update the chatMessage

		chatMessage
			.setModerator (myUser)
			.setStatus (ChatMessageStatus.moderatorRejected)
			.setEditedText (null);

		// and send help message

		chatHelpLogic.sendHelpMessage (
			myUser,
			chatMessage.getFromUser (),
			messageParam,
			chatMessage.getThreadId (),
			null);

		// inc rejection count

		chatMessageLogic.chatUserRejectionCountInc (
			chatMessage.getFromUser (),
			chatMessage.getThreadId ());

		transaction.commit ();

		requestContext.addNotice ("Rejection sent");

		return responder ("queueHomeResponder");

	}

}
