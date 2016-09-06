package wbs.clients.apn.chat.core.console;

import wbs.clients.apn.chat.user.core.model.ChatUserEditReason;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;

public
interface ChatConsoleLogic {

	String textForChatUser (
			ChatUserRec chatUser);

	@Deprecated
	String tdForChatUserTypeShort (
			ChatUserRec chatUser);

	@Deprecated
	String tdForChatUserGenderShort (
			ChatUserRec chatUser);

	@Deprecated
	String tdForChatUserOrientShort (
			ChatUserRec chatUser);

	@Deprecated
	String tdsForChatUserTypeGenderOrientShort (
			ChatUserRec chatUser);

	String textForChatUserInfoStatus (
			ChatUserInfoStatus status);

	String textForChatUserEditReason (
			ChatUserEditReason reason);

	@Deprecated
	String selectForChatUserEditReason (
			String name,
			String value);

}
