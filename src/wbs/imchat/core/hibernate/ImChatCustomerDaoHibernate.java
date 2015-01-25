package wbs.imchat.core.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.imchat.core.model.ImChatCustomerDao;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatCustomerSearch;

public
class ImChatCustomerDaoHibernate
	extends HibernateDao
	implements ImChatCustomerDao {

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
