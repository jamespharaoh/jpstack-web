package wbs.smsapps.orderer.hibernate;

import java.util.List;

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
			OrdererRec orderer,
			NumberRec number) {

		return findMany (
			OrdererOrderRec.class,

			createQuery (
				"FROM OrdererOrderRec ordererOrder " +
				"WHERE ordererOrder.orderer = :orderer " +
					"AND ordererOrder.number = :number")

			.setEntity (
				"orderer",
				orderer)

			.setEntity (
				"number",
				number)

			.list ());

	}

}
