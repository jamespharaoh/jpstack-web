package wbs.integrations.oxygen8.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.oxygen8.model.Oxygen8ConfigRec;
import wbs.integrations.oxygen8.model.Oxygen8NetworkDao;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.sms.network.model.NetworkRec;

public
class Oxygen8NetworkDaoHibernate
	extends HibernateDao
	implements Oxygen8NetworkDao {

	@Override
	public
	Oxygen8NetworkRec findByChannel (
			@NonNull Oxygen8ConfigRec oxygen8Config,
			@NonNull String channel) {

		return findOne (
			"findByChannel (oxygen8Config, channel)",
			Oxygen8NetworkRec.class,

			createCriteria (
				Oxygen8NetworkRec.class,
				"_oxygen8Network")

			.add (
				Restrictions.eq (
					"_oxygen8Network.oxygen8Config",
					oxygen8Config))

			.add (
				Restrictions.eq (
					"_oxygen8Network.channel",
					channel))

		);

	}

	@Override
	public
	Oxygen8NetworkRec find (
			@NonNull Oxygen8ConfigRec oxygen8Config,
			@NonNull NetworkRec network) {

		return findOne (
			"find (oxygen8Config, network)",
			Oxygen8NetworkRec.class,

			createCriteria  (
				Oxygen8NetworkRec.class,
				"_oxygen8Network")

			.add (
				Restrictions.eq (
					"_oxygen8Network.oxygen8Config",
					oxygen8Config))

			.add (
				Restrictions.eq (
					"_oxygen8Network.network",
					network))

		);

	}

}
