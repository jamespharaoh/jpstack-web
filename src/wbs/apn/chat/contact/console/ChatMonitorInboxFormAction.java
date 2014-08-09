package wbs.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatBlockObjectHelper;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.gsm.Gsm;

@PrototypeComponent ("chatMonitorInboxFormAction")
public
class ChatMonitorInboxFormAction
	extends ConsoleAction {

	@Inject
	ChatBlockObjectHelper chatBlockHelper;

	@Inject
	ChatContactObjectHelper chatContactHelper;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	PrivChecker privChecker;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatMonitorInboxFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		// get stuff
		int monitorInboxId = requestContext.stuffInt ("chatMonitorInboxId");

		// get params

		String text =
			requestContext.parameter ("text");

		boolean ignore =
			requestContext.parameter ("ignore") != null;

		boolean note =
			requestContext.parameter ("sendAndNote") != null;

		// check params

		if (! ignore) {

			if (text.length () == 0) {

				requestContext.addError (
					"Please enter a message");

				return null;

			}

			if (! Gsm.isGsm (text)) {

				requestContext.addError (
					"Message text is invalid");

				return null;

			}

			if (Gsm.length (text)
					> ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
						* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES) {

				requestContext.addError (
					"Message text is too long");

				return null;

			}

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.find (
				monitorInboxId);

		ChatUserRec monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		ChatUserRec userChatUser =
			chatMonitorInbox.getUserChatUser ();

		ChatRec chat =
			userChatUser.getChat ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		if (ignore) {

			// check if they can ignore

			if (! privChecker.can (
					chat,
					"manage")) {

				requestContext.addError (
					"Can't ignore");

				return null;

			}

		} else {

			if (Gsm.length (text)
					< chat.getMinMonitorMessageLength ()) {

				requestContext.addError (
					stringFormat (
						"Message text is too short (minimum %d)",
						chat.getMinMonitorMessageLength ()));

				return null;
			}

			ChatBlockRec chatBlock =
				chatBlockHelper.find (
					userChatUser,
					monitorChatUser);

			boolean blocked =
				chatBlock != null
				|| userChatUser.getBlockAll ();

			boolean deleted =
				chatUserLogic.deleted (userChatUser);

			// create a chat message

			TextRec textRec =
				textHelper.findOrCreate (text);

			ChatMessageRec chatMessage =
				objectManager.insert (
					new ChatMessageRec ()
						.setChat (chat)
						.setFromUser (monitorChatUser)
						.setToUser (userChatUser)
						.setOriginalText (textRec)
						.setEditedText (
							blocked
								? null
								: textRec)
						.setStatus (
							blocked
								? ChatMessageStatus.blocked
								: ChatMessageStatus.sent)
						.setSender (myUser));

			// update contact entry, set monitor warning if first message

			ChatContactRec chatContact =
				chatContactHelper.findOrCreate (
					monitorChatUser,
					userChatUser);

			if (chatContact.getLastDeliveredMessageTime () == null
					&& ! monitorChatUser.getStealthMonitor ())
				chatMessage.setMonitorWarning (true);

			chatContact.setLastDeliveredMessageTime (
				transaction.timestamp ());

			// update chat user stats

			if (! (blocked || deleted)) {

				userChatUser.setLastReceive (
					transaction.timestamp ());

			}

			// send message

			if (! (blocked || deleted)) {

				chatMessageLogic.chatMessageDeliverToUser (
					chatMessage);

			}

			// charge

			if (! (blocked || deleted)) {

				chatCreditLogic.userReceiveSpend (
					userChatUser,
					1);

			}

			// update monitor last action

			monitorChatUser
				.setLastAction (transaction.timestamp ());

			// create a note

			if (note) {

				objectManager.insert (
					new ChatContactNoteRec ()
						.setUser (userChatUser)
						.setMonitor (monitorChatUser)
						.setNotes (text)
						.setTimestamp (transaction.timestamp ())
						.setConsoleUser (myUser)
						.setChat (userChatUser.getChat ()));

			}

		}

		// and delete the monitor inbox entry

		queueLogic.processQueueItem (
			chatMonitorInbox.getQueueItem (),
			myUser);

		objectManager.remove (
			chatMonitorInbox);

		// commit transaction

		transaction.commit ();

		// add notice

		if (ignore) {
			requestContext.addNotice ("Message ignored");
		} else if (note) {
			requestContext.addNotice ("Message sent and note added");
		} else {
			requestContext.addNotice ("Message sent");
		}

		// and return

		return responder ("queueHomeResponder");

	}

}