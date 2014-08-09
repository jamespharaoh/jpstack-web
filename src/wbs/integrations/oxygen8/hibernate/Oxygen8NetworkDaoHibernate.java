package wbs.integrations.oxygen8.hibernate;

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
			Oxygen8ConfigRec oxygen8Config,
			String channel) {

		return findOne (
			Oxygen8NetworkRec.class,

			createQuery (
				"FROM Oxygen8NetworkRec oxygen8Network " +
				"WHERE oxygen8Network.oxygen8Config = :oxygen8Config " +
					"AND oxygen8Network.channel = :channel")

			.setEntity (
				"oxygen8Config",
				oxygen8Config)

			.setString (
				"channel",
				channel)

			.list ());

	}

	@Override
	public
	Oxygen8NetworkRec find (
			Oxygen8ConfigRec oxygen8Config,
			NetworkRec network) {

		return findOne (
			Oxygen8NetworkRec.class,

			createQuery (
				"FROM Oxygen8NetworkRec oxygen8Network " +
				"WHERE oxygen8Network.oxygen8Config = :oxygen8Config " +
					"AND oxygen8Network.network = :network")

			.setEntity (
				"oxygen8Config",
				oxygen8Config)

			.setEntity (
				"network",
				network)

			.list ());

	}

}
