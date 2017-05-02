package wbs.apn.chat.user.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.user.core.model.ChatUserAlarmDao;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatUserAlarmDaoHibernate
	extends HibernateDao
	implements ChatUserAlarmDao {

	// singleton dependences

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatUserAlarmRec> findPending (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPending");

		) {

			return findMany (
				transaction,
				ChatUserAlarmRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	ChatUserAlarmRec find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserRec monitorChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				ChatUserAlarmRec.class,

				createCriteria (
					transaction,
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

}
