package wbs.clients.apn.chat.contact.logic;

import java.util.List;

import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;

public
interface ChatMessageLogic {

	boolean chatMessageDeliverViaSms (
			ChatMessageRec chatMessage,
			String text);

	boolean chatMessageDeliverToUser (
			ChatMessageRec chatMessage);

	boolean chatMessageDeliverViaJigsaw (
			ChatMessageRec chatMessage,
			String text);

	void chatMessageDeliver (
			ChatMessageRec chatMessage);

	boolean chatMessageIsRecentDupe (
			ChatUserRec fromUser,
			ChatUserRec toUser,
			TextRec originalText);

	String chatMessagePrependWarning (
			ChatMessageRec chatMessage);

	String chatMessageSendFromUser (
			ChatUserRec fromUser,
			ChatUserRec toUser,
			String text,
			Integer threadId,
			ChatMessageMethod source,
			List<MediaRec> medias);

	void chatMessageSendFromUserPartTwo (
			ChatMessageRec chatMessage);

	ApprovalResult checkForApproval (
			ChatRec chat,
			String message);

	ChatMonitorInboxRec findOrCreateChatMonitorInbox (
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
	 *            ChatUserRec of chat user to inc count of
	 * @param threadId
	 *            threadId of existing message thread to associate allMessages with
	 */
	void chatUserRejectionCountInc (
			ChatUserRec chatUser,
			Integer threadId);

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
