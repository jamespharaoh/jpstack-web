package wbs.applications.imchat.hibernate;

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
