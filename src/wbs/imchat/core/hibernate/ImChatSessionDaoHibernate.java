package wbs.imchat.core.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.imchat.core.model.ImChatSessionDao;
import wbs.imchat.core.model.ImChatSessionRec;

public
class ImChatSessionDaoHibernate
	extends HibernateDao
	implements ImChatSessionDao {

	@Override
	public
	ImChatSessionRec findBySecret (
			String secret) {

		return findOne (
			ImChatSessionRec.class,

			createCriteria (
				ImChatSessionRec.class,
				"_imChatSession")

			.add (
				Restrictions.eq (
					"_imChatSession.secret",
					secret))

			.list ());

	}

}
