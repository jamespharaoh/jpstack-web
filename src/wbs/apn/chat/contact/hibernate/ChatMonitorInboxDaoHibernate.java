package wbs.apn.chat.contact.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.contact.model.ChatMonitorInboxDao;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatMonitorInboxDaoHibernate
	extends HibernateDao
	implements ChatMonitorInboxDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatMonitorInboxRec find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec monitorChatUser,
			@NonNull ChatUserRec userChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				ChatMonitorInboxRec.class,

				createCriteria (
					transaction,
					ChatMonitorInboxRec.class,
					"_chatMonitorInbox")

				.add (
					Restrictions.eq (
						"_chatMonitorInbox.monitorChatUser",
						monitorChatUser))

				.add (
					Restrictions.eq (
						"_chatMonitorInbox.userChatUser",
						userChatUser))

			);

		}

	}

}
