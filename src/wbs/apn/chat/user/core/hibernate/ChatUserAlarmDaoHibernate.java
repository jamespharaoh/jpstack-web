package wbs.apn.chat.user.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;

import wbs.apn.chat.user.core.model.ChatUserAlarmDao;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatUserAlarmDaoHibernate
	extends HibernateDao
	implements ChatUserAlarmDao {

	@Override
	public
	List<ChatUserAlarmRec> findPending (
			@NonNull Instant now) {

		return findMany (
			"findPending (now)",
			ChatUserAlarmRec.class,

			createCriteria (
				ChatUserAlarmRec.class,
				"_chatUserAlarm")

			.createAlias (
				"_chatUserAlarm.chatUser",
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat")

			.add (
				Restrictions.eq (
					"_chat.deleted",
					false))

			.add (
				Restrictions.le (
					"_chatUserAlarm.alarmTime",
					now))

		);

	}

	@Override
	public
	ChatUserAlarmRec find (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserRec monitorChatUser) {

		return findOneOrNull (
			"find (chatUser, monitorChatUser)",
			ChatUserAlarmRec.class,

			createCriteria (
				ChatUserAlarmRec.class,
				"_chatUserAlarm")

			.add (
				Restrictions.eq (
					"_chatUserAlarm.chatUser",
					chatUser))

			.add (
				Restrictions.eq (
					"_chatUserAlarm.monitorChatUser",
					monitorChatUser))

		);

	}

}
