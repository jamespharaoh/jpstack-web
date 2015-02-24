package wbs.imchat.core.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.imchat.core.model.ImChatPurchaseDao;
import wbs.imchat.core.model.ImChatPurchaseRec;

public
class ImChatPurchaseDaoHibernate
	extends HibernateDao
	implements ImChatPurchaseDao {

	@Override
	public
	ImChatPurchaseRec findByToken (
			String token) {

		return findOne (
			ImChatPurchaseRec.class,

			createCriteria (
				ImChatPurchaseRec.class,
				"_imChatPurchase")

			.add (
				Restrictions.eq (
					"_imChatPurchase.token",
					token))

			.list ());

	}

}
