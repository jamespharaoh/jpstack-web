package shn.show.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import shn.show.model.ShnShowDaoMethods;
import shn.show.model.ShnShowRec;
import shn.show.model.ShnShowSearch;

public
class ShnShowDaoHibernate
	extends HibernateDao
	implements ShnShowDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShowSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ShnShowRec.class,
					"_show")

				.createAlias (
					"_show.type",
					"_showType")

				.createAlias (
					"_showType.database",
					"_shnDatabase")

			;

			if (
				isNotNull (
					search.databaseId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_shnDatabase.id",
						search.databaseId ()));

			}

			if (
				isNotNull (
					search.showTypeId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_showType.id",
						search.showTypeId ()));

			}

			if (
				isNotNull (
					search.description ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_show.description",
						stringFormat (
							"%%%s%%",
							search.description ())));

			}

			if (
				isNotNull (
					search.deleted ())
			) {

				criteria.add (
					Restrictions.eq (
						"_show.deleted",
						search.deleted ()));

			}

			if (
				isNotNull (
					search.startTime ())
			) {

				if (search.startTime ().hasStart ()) {

					criteria.add (
						Restrictions.ge (
							"_show.startTime",
							search.startTime ().start ()));

				}

				if (search.startTime ().hasEnd ()) {

					criteria.add (
						Restrictions.lt (
							"_show.startTime",
							search.startTime ().end ()));

				}

			}

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
