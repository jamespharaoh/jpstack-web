package wbs.smsapps.forwarder.hibernate;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.framework.hibernate.TimestampWithTimezoneUserType;
import wbs.smsapps.forwarder.model.ForwarderMessageInDaoMethods;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageInDaoHibernate
	extends HibernateDao
	implements ForwarderMessageInDaoMethods {

	@Override
	public
	ForwarderMessageInRec findNext (
			Instant now,
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

			.setParameter (
				"now",
				now,
				TimestampWithTimezoneUserType.INSTANCE)

			.setMaxResults (1)

			.list ());

	}

	@Override
	public
	List<ForwarderMessageInRec> findNexts (
			Instant now,
			int maxResults) {

		return findMany (
			ForwarderMessageInRec.class,

			createQuery (
				"FROM ForwarderMessageInRec forwarderMessageIn " +
				"WHERE forwarderMessageIn.sendQueue = true " +
					"AND forwarderMessageIn.retryTime < :now")

			.setParameter (
				"now",
				now,
				TimestampWithTimezoneUserType.INSTANCE)

			.setMaxResults (
				maxResults)

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
