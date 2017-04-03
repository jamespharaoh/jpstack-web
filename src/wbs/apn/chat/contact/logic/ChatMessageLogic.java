package wbs.apn.chat.contact.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;

import wbs.sms.message.core.model.MessageRec;

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

	void chatUserRejectionCountInc (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			MessageRec message);

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
