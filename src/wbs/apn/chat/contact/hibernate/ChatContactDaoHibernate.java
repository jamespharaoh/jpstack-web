package wbs.apn.chat.contact.hibernate;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.FlushMode;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.contact.model.ChatContactDao;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatContactDaoHibernate
	extends HibernateDao
	implements ChatContactDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <ChatContactRec> findNoCache (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findNoCache");

		) {

			return optionalFromNullable (
				findOneOrNull (
					transaction,
					ChatContactRec.class,

				createCriteria(
					transaction,
					ChatContactRec.class,
					"_chatContact")

				.add (
					Restrictions.eq (
						"_chatContact.fromUser",
						fromChatUser))

				.add (
					Restrictions.eq (
						"_chatContact.toUser",
						toChatUser))

				.setFlushMode (
					FlushMode.MANUAL)

			));

		}

	}

}
