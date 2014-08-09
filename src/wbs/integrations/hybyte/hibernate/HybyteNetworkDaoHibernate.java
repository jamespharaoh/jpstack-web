package wbs.integrations.hybyte.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.hybyte.model.HybyteNetworkDao;
import wbs.integrations.hybyte.model.HybyteNetworkRec;

public
class HybyteNetworkDaoHibernate
	extends HibernateDao
	implements HybyteNetworkDao {

	@Override
	public
	HybyteNetworkRec findByInText (
			String inText) {

		return findOne (
			HybyteNetworkRec.class,

			createQuery (
				"FROM HybyteNetworkRec hybyteNetwork " +
				"WHERE hybyteNetwork.inText = :inText")

			.setString (
				"inText",
				inText)

			.list ());

	}

}
