package wbs.smsapps.orderer.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.orderer.model.OrdererOrderDao;
import wbs.smsapps.orderer.model.OrdererOrderRec;
import wbs.smsapps.orderer.model.OrdererRec;

public
class OrdererOrderDaoHibernate
	extends HibernateDao
	implements OrdererOrderDao {

	@Override
	public
	List<OrdererOrderRec> find (
			@NonNull OrdererRec orderer,
			@NonNull NumberRec number) {

		return findMany (
			"find (orderer, number)",
			OrdererOrderRec.class,

			createCriteria (
				OrdererOrderRec.class,
				"_ordererOrder")

			.add (
				Restrictions.eq (
					"_ordererOrder.orderer",
					orderer))

			.add (
				Restrictions.eq (
					"_ordererOrder.number",
					number))

		);

	}

}
