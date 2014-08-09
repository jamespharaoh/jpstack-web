package wbs.smsapps.forwarder.hibernate;

import java.util.Date;
import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec.ForwarderMessageInDaoMethods;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageInDaoHibernate
	extends HibernateDao
	implements ForwarderMessageInDaoMethods {

	@Override
	public
	ForwarderMessageInRec findNext (
			ForwarderRec forwarder) {

		return findOne (
			ForwarderMessageInRec.class,

			createQuery(
				"FROM ForwarderMessageInRec forwarderMessageIn " +
				"WHERE forwarderMessageIn.forwarder = :forwarder " +
					"AND forwarderMessageIn.pending = true " +
					"AND (forwarderMessageIn.borrowedTime IS NULL " +
						"OR forwarderMessageIn.borrowedTime < :now) " +
				"ORDER BY forwarderMessageIn.createdTime")

			.setEntity (
				"forwarder",
				forwarder)

			.setTimestamp (
				"now",
				new Date ())

			.setMaxResults (1)

			.list ());

	}

	@Override
	public
	List<ForwarderMessageInRec> findNexts (
			int maxResults) {

		return findMany (
			ForwarderMessageInRec.class,

			createQuery (
				"FROM ForwarderMessageInRec forwarderMessageIn " +
				"WHERE forwarderMessageIn.sendQueue = true " +
					"AND forwarderMessageIn.retryTime < :now")

			.setTimestamp (
				"now",
				new Date ())

			.setMaxResults (maxResults)

			.list ());

	}

	@Override
	public
	List<ForwarderMessageInRec>
	findPendingLimit (
			ForwarderRec forwarder,
			int maxResults) {

		return findMany (
			ForwarderMessageInRec.class,

			createQuery (
				"FROM ForwarderMessageInRec forwarderMessageIn " +
				"WHERE forwarderMessageIn.pending = true " +
					"AND forwarderMessageIn.forwarder = :forwarder " +
				"ORDER BY forwarderMessageIn.id")

			.setEntity (
				"forwarder",
				forwarder)

			.setMaxResults (maxResults)

			.list ());

	}

}
