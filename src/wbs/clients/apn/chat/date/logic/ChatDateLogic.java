package wbs.clients.apn.chat.date.logic;

import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;

public interface ChatDateLogic {

	void userDateStuff (
			ChatUserRec chatUser,
			UserRec user,
			MessageRec message,
			ChatUserDateMode dateMode,
			Integer radius,
			Integer startHour,
			Integer endHour,
			Integer dailyMax,
			boolean sendMessage);

	void userDateStuff (
			ChatUserRec chatUser,
			UserRec user,
			MessageRec message,
			ChatUserDateMode dateMode,
			boolean sendMessage);

	void chatUserDateJoinHint (
			ChatUserRec chatUser);

	void chatUserDateUpgradeHint (
			ChatUserRec chatUser);

}
