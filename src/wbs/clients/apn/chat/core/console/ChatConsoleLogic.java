package wbs.clients.apn.chat.core.console;

import wbs.clients.apn.chat.user.core.model.ChatUserEditReason;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;

public
interface ChatConsoleLogic {

	String textForChatUser (
			ChatUserRec chatUser);

	String tdForChatUserTypeShort (
			ChatUserRec chatUser);

	String tdForChatUserGenderShort (
			ChatUserRec chatUser);

	String tdForChatUserOrientShort (
			ChatUserRec chatUser);

	String tdsForChatUserTypeGenderOrientShort (
			ChatUserRec chatUser);

	String textForChatUserInfoStatus (
			ChatUserInfoStatus status);

	String textForChatUserEditReason (
			ChatUserEditReason reason);

	String selectForChatUserEditReason (
			String name,
			String value);

	String selectForGender (
			String name,
			String value);

	String selectForOrient (
			String name,
			String value);

}
