package wbs.smsapps.broadcast.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.broadcast.model.BroadcastNumberDao;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;

public
class BroadcastNumberDaoHibernate
	extends HibernateDao
	implements BroadcastNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	BroadcastNumberRec find (
			@NonNull Transaction parentTransaction,
			@NonNull BroadcastRec broadcast,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				BroadcastNumberRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <Optional <BroadcastNumberRec>> findMany (
			@NonNull Transaction parentTransaction,
			@NonNull BroadcastRec broadcast,
			@NonNull List <NumberRec> numbers) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMany");

		) {

			if (
				collectionIsEmpty (
					numbers)
			) {
				return emptyList ();
			}

			List <BroadcastNumberRec> broadcastNumbers =
				findMany (
					transaction,
					BroadcastNumberRec.class,

				createCriteria (
					transaction,
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
				numbers,
				number ->
					mapItemForKey (
						broadcastNumbersMap,
						number));

		}

	}

	@Override
	public
	List <BroadcastNumberRec> findAcceptedLimit (
			@NonNull Transaction parentTransaction,
			@NonNull BroadcastRec broadcast,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAcceptedLimit");

		) {

			return findMany (
				transaction,
				BroadcastNumberRec.class,

				createCriteria (
					transaction,
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
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
