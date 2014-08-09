package wbs.smsapps.broadcast.hibernate;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.broadcast.model.BroadcastDao;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

public
class BroadcastDaoHibernate
	extends HibernateDao
	implements BroadcastDao {

	@Override
	public
	List<BroadcastRec> findSending () {

		return findMany (
			BroadcastRec.class,

			createQuery (
				"FROM BroadcastRec broadcast " +
				"WHERE broadcast.state = :broadcastSendingState")

			.setParameter (
				"broadcastSendingState",
				BroadcastState.sending)

			.list ());

	}

	@Override
	public
	List<BroadcastRec> findScheduled (
			Instant scheduledTime) {

		return findMany (
			BroadcastRec.class,

			createQuery (
				"FROM BroadcastRec broadcast " +
				"WHERE broadcast.state = :broadcastScheduledState " +
					"AND broadcast.scheduledTime <= :scheduledTime")

			.setParameter (
				"broadcastScheduledState",
				BroadcastState.scheduled)

			.setParameter (
				"scheduledTime",
				scheduledTime)

			.list ());

	}

}
