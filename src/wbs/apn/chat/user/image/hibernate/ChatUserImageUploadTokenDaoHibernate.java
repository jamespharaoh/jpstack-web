package wbs.apn.chat.user.image.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenDao;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;

public
class ChatUserImageUploadTokenDaoHibernate
	extends HibernateDao
	implements ChatUserImageUploadTokenDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatUserImageUploadTokenRec findByToken (
			@NonNull Transaction parentTransaction,
			@NonNull String token) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByToken");

		) {

			return findOneOrNull (
				transaction,
				ChatUserImageUploadTokenRec.class,

				createCriteria (
					transaction,
					ChatUserImageUploadTokenRec.class)

				.add (
					Restrictions.eq (
						"token",
						token))

			);

		}

	}

}
