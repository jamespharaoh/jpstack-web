package wbs.smsapps.broadcast.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.broadcast.model.BroadcastNumberDao;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;

public
class BroadcastNumberDaoHibernate
	extends HibernateDao
	implements BroadcastNumberDao {

	@Override
	public
	BroadcastNumberRec findByBroadcastAndNumber (
			int broadcastId,
			int numberId) {

		return findOne (
			BroadcastNumberRec.class,

			createQuery (
				"FROM BroadcastNumberRec broadcastNumber " +
				"WHERE broadcastNumber.broadcast.id = :broadcastId " +
					"AND broadcastNumber.number.id = :numberId")

			.setInteger (
				"broadcastId",
				broadcastId)

			.setInteger (
				"numberId",
				numberId)

			.list ());

	}

	@Override
	public
	List<BroadcastNumberRec> findAcceptedByBroadcastLimit (
			int broadcastId,
			int limit) {

		return findMany (
			BroadcastNumberRec.class,

			createQuery (
				"FROM BroadcastNumberRec broadcastNumber " +
				"WHERE broadcastNumber.broadcast.id = :broadcastId " +
					"AND broadcastNumber.state = :broadcastNumberAcceptedState")

			.setInteger (
				"broadcastId",
				broadcastId)

			.setParameter (
				"broadcastNumberAcceptedState",
				BroadcastNumberState.accepted)

			.setMaxResults (
				limit)

			.list ());

	}

}
