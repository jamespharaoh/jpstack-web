package wbs.integrations.mig.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.mig.model.MigNetworkDao;
import wbs.integrations.mig.model.MigNetworkRec;

public
class MigNetworkDaoHibernate
	extends HibernateDao
	implements MigNetworkDao {

	@Override
	public
	MigNetworkRec findBySuffix (
			String suffix) {

		return findOne (
			MigNetworkRec.class,

			createQuery (
				"FROM MigNetworkRec network " +
				"WHERE network.suffix = :suffix " +
					"AND virtual = false")

			.setString (
				"suffix",
				suffix)

			.list ());

	}

}
