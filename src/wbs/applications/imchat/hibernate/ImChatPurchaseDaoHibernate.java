package wbs.applications.imchat.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.applications.imchat.model.ImChatPurchaseDao;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.framework.hibernate.HibernateDao;

public
class ImChatPurchaseDaoHibernate
	extends HibernateDao
	implements ImChatPurchaseDao {

	@Override
	public
	ImChatPurchaseRec findByToken (
			@NonNull String token) {

		return findOne (
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
