package wbs.apn.chat.contact.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.contact.model.ChatContactNoteDao;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatContactNoteDaoHibernate
	extends HibernateDao
	implements ChatContactNoteDao {

	// singleton depedencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatContactNoteRec> findByTimestamp (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByTimestamp");

		) {

			return findMany (
				transaction,
				ChatContactNoteRec.class,

				createCriteria (
					transaction,
					ChatContactNoteRec.class,
					"_chatContactNote")

				.add (
					Restrictions.eq (
						"_chatContactNote.chat",
						chat))

				.add (
					Restrictions.ge (
						"_chatContactNote.timestamp",
						timestamp.getStart ()))

				.add (
					Restrictions.lt (
						"_chatContactNote.timestamp",
						timestamp.getEnd ()))

				.addOrder (
					Order.asc (
						"_chatContactNote.timestamp"))

			);

		}

	}

	@Override
	public
	List <ChatContactNoteRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec userChatUser,
			@NonNull ChatUserRec monitorChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findMany (
				transaction,
				ChatContactNoteRec.class,

				createCriteria (
					transaction,
					ChatContactNoteRec.class,
					"_chatContactNote")

				.add (
					Restrictions.eq (
						"_chatContactNote.user",
						userChatUser))

				.add (
					Restrictions.eq (
						"_chatContactNote.monitor",
						monitorChatUser))

				.addOrder (
					Order.desc (
						"_chatContactNote.pegged"))

				.addOrder (
					Order.asc (
						"_chatContactNote.timestamp"))

			);

		}

	}


}
