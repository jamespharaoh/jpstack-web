package wbs.smsapps.broadcast.hibernate;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

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

		return findOneOrNull (
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
	List <Optional <BroadcastNumberRec>> findMany (
			@NonNull BroadcastRec broadcast,
			@NonNull List <NumberRec> numbers) {

		List <BroadcastNumberRec> broadcastNumbers =
			findMany (
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
				Restrictions.in (
					"_broadcastNumber.number",
					numbers))

		);

		Map <NumberRec, BroadcastNumberRec> broadcastNumbersMap =
			mapWithDerivedKey (
				broadcastNumbers,
				BroadcastNumberRec::getNumber);

		return iterableMapToList (
			number -> mapItemForKey (
				broadcastNumbersMap,
				number),
			numbers);

	}

	@Override
	public
	List <BroadcastNumberRec> findAcceptedLimit (
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
