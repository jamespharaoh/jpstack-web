package wbs.apn.chat.date.logic;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatDateLogic {

	void userDateStuff (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <UserRec> user,
			Optional <MessageRec> message,
			ChatUserDateMode dateMode,
			Long radius,
			Long startHour,
			Long endHour,
			Long dailyMax,
			boolean sendMessage);

	default
	void userDateStuff (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional <UserRec> user,
			@NonNull Optional <MessageRec> message,
			ChatUserDateMode dateMode,
			boolean sendMessage) {

		userDateStuff (
			parentTransaction,
			chatUser,
			user,
			message,
			dateMode,
			chatUser.getDateRadius (),
			chatUser.getDateStartHour (),
			chatUser.getDateEndHour (),
			chatUser.getDateDailyMax (),
			sendMessage);

	}

	void chatUserDateJoinHint (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void chatUserDateUpgradeHint (
			Transaction parentTransaction,
			ChatUserRec chatUser);

}
