package wbs.applications.imchat.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.applications.imchat.model.ImChatCustomerDao;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatCustomerSearch;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ImChatCustomerDaoHibernate
	extends HibernateDao
	implements ImChatCustomerDao {

	@Override
	public
	ImChatCustomerRec findByEmail (
			ImChatRec imChat,
			String email) {

		return findOne (
			ImChatCustomerRec.class,

			createCriteria (
				ImChatCustomerRec.class,
				"_imChatCustomer")

			.add (
				Restrictions.eq (
					"_imChatCustomer.imChat",
					imChat))

			.add (
				Restrictions.eq (
					"_imChatCustomer.email",
					email))

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			ImChatCustomerSearch imChatCustomerSearch) {

		Criteria criteria =

			createCriteria (
				ImChatCustomerRec.class,
				"_imChatCustomer")

			.createAlias (
				"_imChatCustomer.imChat",
				"_imChat");

		if (imChatCustomerSearch.getImChatId () != null) {

			criteria.add (
				Restrictions.eq (
					"_imChat.id",
					imChatCustomerSearch.getImChatId ()));

		}

		if (imChatCustomerSearch.getCode () != null) {

			criteria.add (
				Restrictions.eq (
					"_imChatCustomer.code",
					imChatCustomerSearch.getCode ()));

		}

		// add default order

		criteria

			.addOrder (
				Order.asc ("_imChatCustomer.code"));

		// set to return ids only

		criteria

			.setProjection (
				Projections.id ());

		// perform and return

		return findMany (
			Integer.class,
			criteria.list ());

	}

}
