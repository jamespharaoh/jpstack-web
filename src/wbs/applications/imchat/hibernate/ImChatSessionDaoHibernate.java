package wbs.applications.imchat.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.applications.imchat.model.ImChatSessionDao;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.hibernate.HibernateDao;

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
