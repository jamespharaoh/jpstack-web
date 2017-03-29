package wbs.apn.chat.contact.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatMessageLogic {

	boolean chatMessageDeliverViaSms (
			TaskLogger parentTaskLogger,
			ChatMessageRec chatMessage,
			String text);

	boolean chatMessageDeliverToUser (
			TaskLogger parentTaskLogger,
			ChatMessageRec chatMessage);

	boolean chatMessageDeliverViaJigsaw (
			TaskLogger parentTaskLogger,
			ChatMessageRec chatMessage,
			String text);

	void chatMessageDeliver (
			TaskLogger parentTaskLogger,
			ChatMessageRec chatMessage);

	boolean chatMessageIsRecentDupe (
			ChatUserRec fromUser,
			ChatUserRec toUser,
			TextRec originalText);

	String chatMessagePrependWarning (
			ChatMessageRec chatMessage);

	String chatMessageSendFromUser (
			TaskLogger parentTaskLogger,
			ChatUserRec fromUser,
			ChatUserRec toUser,
			String text,
			Optional <Long> threadId,
			ChatMessageMethod source,
			List<MediaRec> medias);

	void chatMessageSendFromUserPartTwo (
			TaskLogger parentTaskLogger,
			ChatMessageRec chatMessage);

	ApprovalResult checkForApproval (
			ChatRec chat,
			String message);

	ChatMonitorInboxRec findOrCreateChatMonitorInbox (
			TaskLogger parentTaskLogger,
			ChatUserRec monitor,
			ChatUserRec user,
			boolean alarm);

	/**
	 * Increments a chat users rejection count and, when appropriate, triggers
	 * the adult verification as appropriate depending on their network.
	 *
	 * This may be called for already verified users, and monitors (as both
	 * users in a user-to-user message must be verified and this method is still
	 * called for both of them). As such we check for them and skip the
	 * verification process.
	 *
	 * @param chatUser
	 *     ChatUserRec of chat user to inc count of
	 *
	 * @param threadId
	 *     Thread id of existing message thread to associate messages with
	 */
	void chatUserRejectionCountInc (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			Long threadId);

	static
	class ApprovalResult {

		public static
		enum Status {
			clean,
			auto,
			manual
		}

		public
		Status status;

		public
		String message;

	}

}
