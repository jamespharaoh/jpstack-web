package wbs.smsapps.broadcast.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberDao;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;

public
class BroadcastNumberDaoHibernate
	extends HibernateDao
	implements BroadcastNumberDao {

	@Override
	public
	BroadcastNumberRec find (
			@NonNull BroadcastRec broadcast,
			@NonNull NumberRec number) {

		return findOne (
			"find (broadcast, number)",
			BroadcastNumberRec.class,

			createCriteria (
				BroadcastNumberRec.class,
				"_broadcastNumber")

			.add (
				Restrictions.eq (
					"_broadcastNumber.broadcast",
					broadcast))

			.add (
				Restrictions.eq (
					"_broadcastNumber.number",
					number))

		);

	}

	@Override
	public
	List<BroadcastNumberRec> findAcceptedLimit (
			@NonNull BroadcastRec broadcast,
			int maxResults) {

		return findMany (
			"findAcceptedByBroadcastLimit (broadcastId, limit)",
			BroadcastNumberRec.class,

			createCriteria (
				BroadcastNumberRec.class,
				"_broadcastNumber")

			.add (
				Restrictions.eq (
					"_broadcastNumber.broadcast",
					broadcast))

			.add (
				Restrictions.eq (
					"_broadcastNumber.state",
					BroadcastNumberState.accepted))

			.setMaxResults (
				maxResults)

		);

	}

}
