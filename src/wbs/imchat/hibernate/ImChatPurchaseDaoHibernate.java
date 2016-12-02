package wbs.imchat.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.imchat.model.ImChatPurchaseDao;
import wbs.imchat.model.ImChatPurchaseRec;
import wbs.framework.hibernate.HibernateDao;

public
class ImChatPurchaseDaoHibernate
	extends HibernateDao
	implements ImChatPurchaseDao {

	@Override
	public
	ImChatPurchaseRec findByToken (
			@NonNull String token) {

		return findOneOrNull (
			"findByToken (token)",
			ImChatPurchaseRec.class,

			createCriteria (
				ImChatPurchaseRec.class,
				"_imChatPurchase")

			.add (
				Restrictions.eq (
					"_imChatPurchase.token",
					token))

		);

	}

}
