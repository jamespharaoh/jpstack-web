package wbs.imchat.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatPurchaseDao;
import wbs.imchat.model.ImChatPurchaseRec;

public
class ImChatPurchaseDaoHibernate
	extends HibernateDao
	implements ImChatPurchaseDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ImChatPurchaseRec findByToken (
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
				ImChatPurchaseRec.class,

				createCriteria (
					transaction,
					ImChatPurchaseRec.class,
					"_imChatPurchase")

				.add (
					Restrictions.eq (
						"_imChatPurchase.token",
						token))

			);

		}

	}

}
