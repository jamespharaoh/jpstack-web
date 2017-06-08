package wbs.apn.chat.core.logic;

import java.util.List;

import com.google.common.base.Optional;

import org.joda.time.DateTimeZone;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

public
interface ChatMiscLogic {

	List <ChatUserRec> getOnlineMonitorsForOutbound (
			Transaction parentTransaction,
			ChatUserRec thisUser);

	ChatUserRec getOnlineMonitorForOutbound (
			Transaction parentTransaction,
			ChatUserRec thisUser);

	void blockAll (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <MessageRec> message);

	void userJoin (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Boolean sendMessage,
			Optional <Long> threadId,
			Optional <ChatMessageMethod> deliveryMethod);

	void userLogoffWithMessage (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			Boolean automatic);

	void monitorsToTarget (
			Transaction parentTransaction,
			ChatRec chat,
			Gender gender,
			Orient orient,
			Long target);

	void userAutoJoin (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			MessageRec message,
			Boolean sendMessage);

	void chatUserSetName (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			String name,
			Optional <Long> threadId);

	DateTimeZone timezone (
			Transaction parentTransaction,
			ChatRec chat);

}