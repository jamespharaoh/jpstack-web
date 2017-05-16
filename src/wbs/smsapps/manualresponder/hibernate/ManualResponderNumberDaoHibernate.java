package wbs.smsapps.manualresponder.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberDao;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberSearch;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

public
class ManualResponderNumberDaoHibernate
	extends HibernateDao
	implements ManualResponderNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ManualResponderNumberRec find (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				ManualResponderNumberRec.class,

				createCriteria (
					transaction,
					ManualResponderNumberRec.class,
					"_manualResponderNumber")

				.add (
					Restrictions.eq (
						"_manualResponderNumber.manualResponder",
						manualResponder))

				.add (
					Restrictions.eq (
						"_manualResponderNumber.number",
						number))

			);

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderNumberSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ManualResponderNumberRec.class,
					"_manualResponderNumber")

				.createAlias (
					"_manualResponderNumber.number",
					"_number")

				.createAlias (
					"_manualResponderNumber.notesText",
					"_notesText",
					JoinType.LEFT_OUTER_JOIN);

			if (
				isNotNull (
					search.manualResponderId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_manualResponderNumber.manualResponder.id",
						search.manualResponderId ()));

			}

			if (
				isNotNull (
					search.number ())
			) {

				criteria.add (
					Restrictions.eq (
						"_number.number",
						search.number ()));

			}

			if (
				isNotNull (
					search.firstRequest ())
			) {

				criteria.add (
					Restrictions.ge (
						"_manualResponderNumber.firstRequest",
						search.firstRequest ().start ()));

				criteria.add (
					Restrictions.lt (
						"_manualResponderNumber.firstRequest",
						search.firstRequest ().end ()));

			}

			if (
				isNotNull (
					search.lastRequest ())
			) {

				criteria.add (
					Restrictions.ge (
						"_manualResponderNumber.lastRequest",
						search.lastRequest ().start ()));

				criteria.add (
					Restrictions.lt (
						"_manualResponderNumber.lastRequest",
						search.lastRequest ().end ()));

			}

			if (
				isNotNull (
					search.notes ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_notesText.text",
						stringFormat (
							"%%%s%%",
							search.notes ())));

			}

			criteria.addOrder (
				Order.asc (
					"_number.number"));

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
