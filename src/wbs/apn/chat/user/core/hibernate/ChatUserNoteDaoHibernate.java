package wbs.apn.chat.user.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.user.core.model.ChatUserNoteDao;
import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatUserNoteDaoHibernate
	extends HibernateDao
	implements ChatUserNoteDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatUserNoteRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findMany (
				transaction,
				ChatUserNoteRec.class,

				createCriteria (
					transaction,
					ChatUserNoteRec.class,
					"_chatUserNote")

				.add (
					Restrictions.eq (
						"_chatUserNote.chatUser",
						chatUser))

				.addOrder (
					Order.desc (
						"_chatUserNote.timestamp"))

			);

		}

	}

}
