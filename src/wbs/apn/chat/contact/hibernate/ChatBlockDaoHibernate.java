package wbs.apn.chat.contact.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.contact.model.ChatBlockDao;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatBlockDaoHibernate
	extends HibernateDao
	implements ChatBlockDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatBlockRec find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserRec blockedChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				ChatBlockRec.class,

				createCriteria (
					transaction,
					ChatBlockRec.class,
					"_chatBlock")

				.add (
					Restrictions.eq (
						"_chatBlock.chatUser",
						chatUser))

				.add (
					Restrictions.eq (
						"_chatBlock.blockedChatUser",
						blockedChatUser))

			);

		}

	}

}
