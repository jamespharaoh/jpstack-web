package wbs.apn.chat.namednote.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.namednote.model.ChatNoteNameDao;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;

public
class ChatNoteNameDaoHibernate
	extends HibernateDao
	implements ChatNoteNameDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatNoteNameRec> findNotDeleted (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findNotDeleted");

		) {

			return findMany (
				transaction,
				ChatNoteNameRec.class,

				createCriteria (
					transaction,
					ChatNoteNameRec.class,
					"_chatNoteName")

				.add (
					Restrictions.eq (
						"_chatNoteName.chat",
						chat))

				.add (
					Restrictions.eq (
						"_chatNoteName.deleted",
						false))

				.addOrder (
					Order.asc (
						"_chatNoteName.index"))

			);

		}

	}

}
