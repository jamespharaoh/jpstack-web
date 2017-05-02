package wbs.imchat.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatSessionDao;
import wbs.imchat.model.ImChatSessionRec;

public
class ImChatSessionDaoHibernate
	extends HibernateDao
	implements ImChatSessionDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ImChatSessionRec findBySecret (
			@NonNull Transaction parentTransaction,
			@NonNull String secret) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findBySecret");

		) {

			return findOneOrNull (
				transaction,
				ImChatSessionRec.class,

				createCriteria (
					transaction,
					ImChatSessionRec.class,
					"_imChatSession")

				.add (
					Restrictions.eq (
						"_imChatSession.secret",
						secret))

			);

		}

	}

}
