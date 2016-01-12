package wbs.clients.apn.chat.core.logic;

import java.util.List;

import org.joda.time.DateTimeZone;

import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.sms.message.core.model.MessageRec;

public
interface ChatMiscLogic {

	/**
	 * Gets all online monitors who are candidates for a random outbound message
	 * (either in response to a "join" outbound or a "quiet" outbound).
	 *
	 * Monitors considered must have a picture, not be blocked, be compatible
	 * and never previously sent a message to this user.
	 *
	 * @param thisUser
	 *            User message is to be sent to
	 * @return All online monitors who qualify.
	 */
	List<ChatUserRec> getOnlineMonitorsForOutbound (
			ChatUserRec thisUser);

	/**
	 * Get the closest online monitor suitable to use for an outbound message.
	 *
	 * @see getOnlineMoniorsForOutbound for detailed criteria.
	 *
	 * @param thisUser
	 *            User message is to be sent to
	 * @return Monitor to send
	 */
	ChatUserRec getOnlineMonitorForOutbound (
			ChatUserRec thisUser);

	void blockAll (
			ChatUserRec chatUser,
			MessageRec message);

	void userJoin (
			ChatUserRec chatUser,
			boolean sendMessage,
			Long threadId,
			ChatMessageMethod deliveryMethod);

	void userLogoffWithMessage (
			ChatUserRec chatUser,
			Long threadId,
			boolean automatic);

	void monitorsToTarget (
			ChatRec chat,
			Gender gender,
			Orient orient,
			int target);

	void userAutoJoin (
			ChatUserRec chatUser,
			MessageRec message,
			boolean sendMessage);

	void chatUserSetName (
			ChatUserRec chatUser,
			String name,
			Long threadId);

	DateTimeZone timezone (
			ChatRec chat);

}