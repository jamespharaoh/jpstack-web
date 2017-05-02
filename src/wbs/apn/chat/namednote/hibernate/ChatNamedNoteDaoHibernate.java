package wbs.apn.chat.namednote.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.namednote.model.ChatNamedNoteDao;
import wbs.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatNamedNoteDaoHibernate")
public
 class ChatNamedNoteDaoHibernate
	extends HibernateDao
	implements ChatNamedNoteDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatNamedNoteRec find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisChatUser,
			@NonNull ChatUserRec otherChatUser,
			@NonNull ChatNoteNameRec chatNoteName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				ChatNamedNoteRec.class,

				createCriteria (
					transaction,
					ChatNamedNoteRec.class,
					"_chatNamedNote")

				.add (
					Restrictions.eq (
						"_chatNamedNote.thisUser",
						thisChatUser))

				.add (
					Restrictions.eq (
						"_chatNamedNote.otherUser",
						otherChatUser))

				.add (
					Restrictions.eq (
						"_chatNamedNote.chatNoteName",
						chatNoteName))

			);

		}

	}

	@Override
	public
	List <ChatNamedNoteRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec thisChatUser,
			@NonNull ChatUserRec otherChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findMany (
				transaction,
				ChatNamedNoteRec.class,

				createCriteria (
					transaction,
					ChatNamedNoteRec.class,
					"_chatNamedNote")

				.add (
					Restrictions.eq (
						"_chatNamedNote.thisUser",
						thisChatUser))

				.add (
					Restrictions.eq (
						"_chatNamedNote.otherUser",
						otherChatUser))

			);

		}

	}

}
