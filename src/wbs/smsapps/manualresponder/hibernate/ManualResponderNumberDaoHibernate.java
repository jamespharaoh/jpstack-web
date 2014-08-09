package wbs.smsapps.manualresponder.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberDao;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

public
class ManualResponderNumberDaoHibernate
	extends HibernateDao
	implements ManualResponderNumberDao {

	@Override
	public
	ManualResponderNumberRec find (
			ManualResponderRec manualResponder,
			NumberRec number) {

		return findOne (
			ManualResponderNumberRec.class,

			createQuery (
				"FROM ManualResponderNumberRec manualResponderNumber " +
				"WHERE manualResponderNumber.manualResponder = :manualResponder " +
					"AND manualResponderNumber.number = :number")

			.setEntity (
				"manualResponder",
				manualResponder)

			.setEntity (
				"number",
				number)

			.list ());

	}
}
