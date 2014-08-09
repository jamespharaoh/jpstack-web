package wbs.smsapps.forwarder.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.forwarder.model.ForwarderMessageOutDao;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageOutDaoHibernate
	extends HibernateDao
	implements ForwarderMessageOutDao {

	@Override
	public
	ForwarderMessageOutRec findByOtherId (
			ForwarderRec forwarder,
			String otherId) {

		return findOne (
			ForwarderMessageOutRec.class,

			createQuery (
				"FROM ForwarderMessageOutRec fmo " +
				"WHERE fmo.forwarder = :forwarder " +
				"AND fmo.otherId = :otherId")

			.setEntity (
				"forwarder",
				forwarder)

			.setString (
				"otherId",
				otherId)

			.list ());

	}

	@Override
	public
	List<ForwarderMessageOutRec> findPendingLimit (
			ForwarderRec forwarder,
			int maxResults) {

		return findMany (
			ForwarderMessageOutRec.class,

			createQuery (
				"SELECT fmo " +
				"FROM ForwarderMessageOutRec fmo " +
				"WHERE fmo.forwarder = :forwarder " +
					"AND fmo.reportIndexPending IS NOT NULL " +
				"ORDER BY fmo.forwarder, fmo.id")

			.setEntity (
				"forwarder",
				forwarder)

			.setMaxResults (
				maxResults)

			.list ());

	}

}
