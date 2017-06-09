package wbs.apn.chat.core.console;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;

public
interface ChatConsoleLogic {

	String textForChatUser (
			ChatUserRec chatUser);

	String textForChatUserInfoStatus (
			ChatUserInfoStatus status);

	String textForChatUserEditReason (
			ChatUserEditReason reason);

	void writeTdForChatUserTypeShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	void writeTdForChatUserGenderShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	void writeTdForChatUserOrientShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	void writeTdsForChatUserTypeGenderOrientShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	@Deprecated
	void writeSelectForChatUserEditReason (
			FormatWriter formatWriter,
			String name,
			String value);

}
