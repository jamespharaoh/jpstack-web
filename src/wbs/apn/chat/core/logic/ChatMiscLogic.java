package wbs.apn.chat.core.logic;

import java.util.List;

import com.google.common.base.Optional;

import org.joda.time.DateTimeZone;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

public
interface ChatMiscLogic {

	List <ChatUserRec> getOnlineMonitorsForOutbound (
			ChatUserRec thisUser);

	ChatUserRec getOnlineMonitorForOutbound (
			ChatUserRec thisUser);

	void blockAll (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			Optional <MessageRec> message);

	void userJoin (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			boolean sendMessage,
			Long threadId,
			ChatMessageMethod deliveryMethod);

	void userLogoffWithMessage (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			Long threadId,
			boolean automatic);

	void monitorsToTarget (
			ChatRec chat,
			Gender gender,
			Orient orient,
			long target);

	void userAutoJoin (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			MessageRec message,
			boolean sendMessage);

	void chatUserSetName (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			String name,
			Long threadId);

	DateTimeZone timezone (
			ChatRec chat);

}