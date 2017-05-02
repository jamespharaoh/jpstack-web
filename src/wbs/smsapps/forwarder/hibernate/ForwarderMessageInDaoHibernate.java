package wbs.smsapps.forwarder.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.forwarder.model.ForwarderMessageInDaoMethods;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageInDaoHibernate
	extends HibernateDao
	implements ForwarderMessageInDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ForwarderMessageInRec findNext (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now,
			@NonNull ForwarderRec forwarder) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findNext");

		) {

			return findOneOrNull (
				transaction,
				ForwarderMessageInRec.class,

				createCriteria (
					transaction,
					ForwarderMessageInRec.class,
					"_forwarderMessageIn")

				.add (
					Restrictions.eq (
						"_forwarderMessageIn.forwarder",
						forwarder))

				.add (
					Restrictions.eq (
						"_forwarderMessageIn.pending",
						true))

				.add (
					Restrictions.or (

					Restrictions.isNull (
						"_forwarderMessageIn.borrowedTime"),

					Restrictions.le (
						"_forwarderMessageIn.borrowedTime",
						now)

				))

				.addOrder (
					Order.asc (
						"_forwarderMessageIn.createdTime"))

				.setMaxResults (
					1)

			);

		}

	}

	@Override
	public
	List <ForwarderMessageInRec> findNextLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findNextLimit");

		) {

			return findMany (
				transaction,
				ForwarderMessageInRec.class,

				createCriteria (
					transaction,
					ForwarderMessageInRec.class,
					"_forwarderMessageIn")

				.add (
					Restrictions.eq (
						"_forwarderMessageIn.sendQueue",
						true))

				.add (
					Restrictions.lt (
						"_forwarderMessageIn.retryTime",
						now))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

	@Override
	public
	List <ForwarderMessageInRec> findPendingLimit (
			@NonNull Transaction parentTransaction,
			@NonNull ForwarderRec forwarder,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPendingLimit");

		) {

			return findMany (
				transaction,
				ForwarderMessageInRec.class,

				createCriteria (
					transaction,
					ForwarderMessageInRec.class,
					"_forwarderMessageIn")

				.add (
					Restrictions.eq (
						"_forwarderMessageIn.pending",
						true))

				.add (
					Restrictions.eq (
						"_forwarderMessageIn.forwarder",
						forwarder))

				.addOrder (
					Order.asc (
						"_forwarderMessageIn.id"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
