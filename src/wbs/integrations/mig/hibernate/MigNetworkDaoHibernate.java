package wbs.integrations.mig.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull String suffix) {

		return findOne (
			"findBySuffix (suffix)",
			MigNetworkRec.class,

			createCriteria (
				MigNetworkRec.class,
				"_migNetwork")

			.add (
				Restrictions.eq (
					"_migNetwork.suffix",
					suffix))

			.add (
				Restrictions.eq (
					"_migNetwork.virtual",
					false))

		);

	}

}
