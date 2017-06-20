package shn.product.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductDaoMethods;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductSearch;

public
class ShnProductDaoHibernate
	extends HibernateDao
	implements ShnProductDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ShnProductRec.class,
					"_product")

				.createAlias (
					"_product.subCategory",
					"_productSubCategory")

				.createAlias (
					"_productSubCategory.category",
					"_productCategory")

				.createAlias (
					"_productCategory.database",
					"_shnDatabase")

				.createAlias (
					"_product.publicDescription",
					"_publicDescription",
					JoinType.LEFT_OUTER_JOIN)

			;

			if (
				isNotNull (
					search.shnDatabaseId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_shnDatabase.id",
						search.shnDatabaseId ()));

			}

			if (
				isNotNull (
					search.productCategoryId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_productCategory.id",
						search.productCategoryId ()));

			}

			if (
				isNotNull (
					search.productSubCategoryId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_productSubCategory.id",
						search.productSubCategoryId ()));

			}

			if (
				isNotNull (
					search.itemNumber ())
			) {

				criteria.add (
					Restrictions.eq (
						"_product.itemNumber",
						search.itemNumber ()));

			}

			if (
				isNotNull (
					search.description ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_product.description",
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
						"_product.deleted",
						search.deleted ()));

			}

			if (
				isNotNull (
					search.active ())
			) {

				criteria.add (
					Restrictions.eq (
						"_product.active",
						search.active ()));

			}

			if (
				isNotNull (
					search.publicTitle ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_product.publicTitle",
						stringFormat (
							"%%%s%%",
							search.publicTitle ())));

			}

			if (
				isNotNull (
					search.publicDescription ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_publicDescription.text",
						stringFormat (
							"%%%s%%",
							search.publicDescription ())));

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
