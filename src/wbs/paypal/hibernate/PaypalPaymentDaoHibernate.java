package wbs.paypal.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.paypal.model.PaypalPaymentDao;
import wbs.paypal.model.PaypalPaymentRec;

public class PaypalPaymentDaoHibernate
	extends HibernateDao
	implements PaypalPaymentDao {

	@Override
	public
	PaypalPaymentRec findByToken (
			String token) {

		return findOne (
			PaypalPaymentRec.class,

			createCriteria (
				PaypalPaymentRec.class,
				"_paypalPayment")

			.add (
				Restrictions.eq (
					"_paypalPayment.token",
					token))

			.list ());

	}

}
