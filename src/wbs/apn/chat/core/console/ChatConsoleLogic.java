package wbs.apn.chat.core.console;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;

import lombok.NonNull;

import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.utils.string.FormatWriter;

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

	default
	void writeTdForChatUserTypeShort (
			@NonNull ChatUserRec chatUser) {

		writeTdForChatUserTypeShort (
			currentFormatWriter (),
			chatUser);

	}

	void writeTdForChatUserGenderShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	default
	void writeTdForChatUserGenderShort (
			ChatUserRec chatUser) {

		writeTdForChatUserGenderShort (
			currentFormatWriter (),
			chatUser);

	}

	void writeTdForChatUserOrientShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	default
	void writeTdForChatUserOrientShort (
			@NonNull ChatUserRec chatUser) {

		writeTdForChatUserOrientShort (
			currentFormatWriter (),
			chatUser);

	}

	void writeTdsForChatUserTypeGenderOrientShort (
			FormatWriter formatWriter,
			ChatUserRec chatUser);

	default
	void writeTdsForChatUserTypeGenderOrientShort (
			@NonNull ChatUserRec chatUser) {

		writeTdsForChatUserTypeGenderOrientShort (
			currentFormatWriter (),
			chatUser);

	}

	@Deprecated
	void writeSelectForChatUserEditReason (
			FormatWriter formatWriter,
			String name,
			String value);

	@Deprecated
	default
	void writeSelectForChatUserEditReason (
			@NonNull String name,
			@NonNull String value) {

		writeSelectForChatUserEditReason (
			currentFormatWriter (),
			name,
			value);

	}

}
