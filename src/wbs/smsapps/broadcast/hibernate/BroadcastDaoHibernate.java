package wbs.smsapps.broadcast.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
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
			"findSending ()",
			BroadcastRec.class,

			createCriteria (
				BroadcastRec.class,
				"_broadcast")

			.add (
				Restrictions.eq (
					"_broadcast.state",
					BroadcastState.sending))

		);

	}

	@Override
	public
	List<BroadcastRec> findScheduled (
			@NonNull Instant scheduledTime) {

		return findMany (
			"findScheduled (scheduledTime)",
			BroadcastRec.class,

			createCriteria (
				BroadcastRec.class,
				"_broadcast")

			.add (
				Restrictions.eq (
					"_broadcast.state",
					BroadcastState.scheduled))

			.add (
				Restrictions.le (
					"_broadcast.scheduledTime",
					scheduledTime))

		);

	}

}
