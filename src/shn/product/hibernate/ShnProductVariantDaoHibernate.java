package shn.product.hibernate;

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

import shn.product.model.ShnProductVariantDaoMethods;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantSearch;

public
class ShnProductVariantDaoHibernate
	extends HibernateDao
	implements ShnProductVariantDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ShnProductVariantRec.class,
					"_productVariant")

				.createAlias (
					"_productVariant.product",
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
						"_productVariant.itemNumber",
						search.itemNumber ()));

			}

			if (
				isNotNull (
					search.description ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_productVariant.description",
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
						"_productVariant.deleted",
						search.deleted ()));

			}

			if (
				isNotNull (
					search.active ())
			) {

				criteria.add (
					Restrictions.eq (
						"_productVariant.active",
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
					search.stockQuantity ())
			) {

				criteria.add (
					Restrictions.ge (
						"_publicVariant.stockQuantity",
						search.stockQuantity ().getMinimum ()));

				criteria.add (
					Restrictions.le (
						"_publicVariant.stockQuantity",
						search.stockQuantity ().getMaximum ()));

			}

			criteria.addOrder (
				Order.asc (
					"_productVariant.database"));

			criteria.addOrder (
				Order.asc (
					"_productVariant.itemNumber"));

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
