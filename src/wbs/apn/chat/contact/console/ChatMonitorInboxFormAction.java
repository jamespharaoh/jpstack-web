package wbs.apn.chat.contact.console;

import static wbs.sms.gsm.GsmUtils.gsmStringLength;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatBlockObjectHelper;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.sms.gsm.GsmUtils;

@PrototypeComponent ("chatMonitorInboxFormAction")
public
class ChatMonitorInboxFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatBlockObjectHelper chatBlockHelper;

	@SingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	ChatContactNoteConsoleHelper chatContactNoteHelper;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatMonitorInboxFormResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// get stuff

		Long monitorInboxId =
			requestContext.stuffInteger (
				"chatMonitorInboxId");

		// get params

		String text =
			requestContext.parameterRequired (
				"text");

		boolean ignore =
			optionalIsPresent (
				requestContext.parameter (
					"ignore"));

		boolean note =
			optionalIsPresent (
				requestContext.parameter (
					"sendAndNote"));

		// check params

		if (! ignore) {

			if (text.length () == 0) {

				requestContext.addError (
					"Please enter a message");

				return null;

			}

			if (! GsmUtils.gsmStringIsValid (text)) {

				requestContext.addError (
					"Message text is invalid");

				return null;

			}

			if (
				moreThan (
					gsmStringLength (
						text),
					ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
						* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES)
			) {

				requestContext.addError (
					"Message text is too long");

				return null;

			}

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatMonitorInboxFormAction.goReal ()",
				this);

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.findRequired (
				monitorInboxId);

		ChatUserRec monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		ChatUserRec userChatUser =
			chatMonitorInbox.getUserChatUser ();

		ChatRec chat =
			userChatUser.getChat ();

		if (ignore) {

			// check if they can ignore

			if (! privChecker.canRecursive (
					chat,
					"manage")) {

				requestContext.addError (
					"Can't ignore");

				return null;

			}

		} else {

			if (
				lessThan (
					GsmUtils.gsmStringLength (text),
					chat.getMinMonitorMessageLength ())
			) {

				requestContext.addError (
					stringFormat (
						"Message text is too short (minimum %d)",
						integerToDecimalString (
							chat.getMinMonitorMessageLength ())));

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
				chatUserLogic.deleted (
					userChatUser);

			// create a chat message

			TextRec textRec =
				textHelper.findOrCreate (
					text);

			ChatMessageRec chatMessage =
				chatMessageHelper.insert (
					chatMessageHelper.createInstance ()

				.setChat (
					chat)

				.setFromUser (
					monitorChatUser)

				.setToUser (
					userChatUser)

				.setTimestamp (
					transaction.now ())

				.setOriginalText (
					textRec)

				.setEditedText (
					blocked
						? null
						: textRec)

				.setStatus (
					blocked
						? ChatMessageStatus.blocked
						: ChatMessageStatus.sent)

				.setSender (
					userConsoleLogic.userRequired ())

			);

			// update contact entry, set monitor warning if first message

			ChatContactRec chatContact =
				chatContactHelper.findOrCreate (
					monitorChatUser,
					userChatUser);

			if (
				chatContact.getLastDeliveredMessageTime () == null
				&& ! monitorChatUser.getStealthMonitor ()
			) {

				chatMessage

					.setMonitorWarning (
						true);

			}

			chatContact

				.setLastDeliveredMessageTime (
					transaction.now ());

			// update chat user stats

			if (! (blocked || deleted)) {

				userChatUser

					.setLastReceive (
						transaction.now ());

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

				.setLastAction (
					transaction.now ());

			// create a note

			if (note) {

				chatContactNoteHelper.insert (
					chatContactNoteHelper.createInstance ()

					.setUser (
						userChatUser)

					.setMonitor (
						monitorChatUser)

					.setNotes (
						text)

					.setTimestamp (
						transaction.now ())

					.setConsoleUser (
						userConsoleLogic.userRequired ())

					.setChat (
						userChatUser.getChat ())

				);

			}

		}

		// and delete the monitor inbox entry

		queueLogic.processQueueItem (
			chatMonitorInbox.getQueueItem (),
			userConsoleLogic.userRequired ());

		chatMonitorInboxHelper.remove (
			chatMonitorInbox);

		// commit transaction

		transaction.commit ();

		// add notice

		if (ignore) {

			requestContext.addNotice (
				"Message ignored");

		} else if (note) {

			requestContext.addNotice (
				"Message sent and note added");

		} else {

			requestContext.addNotice (
				"Message sent");

		}

		// and return

		return responder (
			"queueHomeResponder");

	}

}