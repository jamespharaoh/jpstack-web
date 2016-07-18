package wbs.integrations.hybyte.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull String inText) {

		return findOne (
			"findByInText (inText)",
			HybyteNetworkRec.class,

			createCriteria (
				HybyteNetworkRec.class,
				"_hybyteNetwork")

			.add (
				Restrictions.eq (
					"_hybyteNetwork.inText",
					inText))

		);

	}

}
