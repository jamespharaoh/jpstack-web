package wbs.smsapps.forwarder.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.forwarder.model.ForwarderMessageOutDao;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageOutDaoHibernate
	extends HibernateDao
	implements ForwarderMessageOutDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ForwarderMessageOutRec findByOtherId (
			@NonNull Transaction parentTransaction,
			@NonNull ForwarderRec forwarder,
			@NonNull String otherId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByOtherId");

		) {

			return findOneOrNull (
				transaction,
				ForwarderMessageOutRec.class,

				createCriteria (
					transaction,
					ForwarderMessageOutRec.class,
					"_forwarderMessageOut")

				.add (
					Restrictions.eq (
						"_forwarderMessageOut.forwarder",
						forwarder))

				.add (
					Restrictions.eq (
						"_forwarderMessageOut.otherId",
						otherId))

			);

		}

	}

	@Override
	public
	List <ForwarderMessageOutRec> findPendingLimit (
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
				ForwarderMessageOutRec.class,

				createCriteria (
					transaction,
					ForwarderMessageOutRec.class,
					"_forwarderMessageOut")

				.add (
					Restrictions.eq (
						"_forwarderMessageOut.forwarder",
						forwarder))

				.add (
					Restrictions.isNotNull (
						"_forwarderMessageOut.reportIndexPending"))

				.addOrder (
					Order.asc (
						"_forwarderMessageOut.forwarder"))

				.addOrder (
					Order.asc (
						"_forwarderMessageOut.id"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
