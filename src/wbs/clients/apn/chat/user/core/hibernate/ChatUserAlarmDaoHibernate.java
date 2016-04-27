package wbs.clients.apn.chat.user.core.hibernate;

import java.util.List;

import org.joda.time.Instant;

import wbs.clients.apn.chat.user.core.model.ChatUserAlarmDao;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.hibernate.TimestampWithTimezoneUserType;

public
class ChatUserAlarmDaoHibernate
	extends HibernateDao
	implements ChatUserAlarmDao {

	@Override
	public
	List<ChatUserAlarmRec> findPending (
			Instant now) {

		return findMany (
			ChatUserAlarmRec.class,

			createQuery (
				"FROM ChatUserAlarmRec cua " +
				"WHERE cua.alarmTime <= :now")

			.setParameter (
				"now",
				now,
				TimestampWithTimezoneUserType.INSTANCE)

			.list ());

	}

	@Override
	public
	ChatUserAlarmRec find (
			ChatUserRec chatUser,
			ChatUserRec monitorChatUser) {

		return findOne (
			ChatUserAlarmRec.class,

			createQuery (
				"FROM ChatUserAlarmRec alarm " +
				"WHERE alarm.chatUser = :chatUser " +
				"AND alarm.monitorChatUser = :monitorChatUser")

			.setEntity (
				"chatUser",
				chatUser)

			.setEntity (
				"monitorChatUser",
				monitorChatUser)

			.list ());

	}

}
