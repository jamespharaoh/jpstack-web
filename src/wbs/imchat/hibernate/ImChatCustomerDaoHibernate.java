package wbs.imchat.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.time.TimeUtils.toInstant;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatCustomerDao;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatCustomerSearch;
import wbs.imchat.model.ImChatRec;

public
class ImChatCustomerDaoHibernate
	extends HibernateDao
	implements ImChatCustomerDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ImChatCustomerRec findByEmail (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatRec imChat,
			@NonNull String email) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByEmail");

		) {

			return findOneOrNull (
				transaction,
				ImChatCustomerRec.class,

				createCriteria (
					transaction,
					ImChatCustomerRec.class,
					"_imChatCustomer")

				.add (
					Restrictions.eq (
						"_imChatCustomer.imChat",
						imChat))

				.add (
					Restrictions.eq (
						"_imChatCustomer.email",
						email))

			);

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatCustomerSearch imChatCustomerSearch) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =

				createCriteria (
					transaction,
					ImChatCustomerRec.class,
					"_imChatCustomer")

				.createAlias (
					"_imChatCustomer.imChat",
					"_imChat");

			if (
				isNotNull (
					imChatCustomerSearch.imChatId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_imChat.id",
						imChatCustomerSearch.imChatId ()));

			}

			if (imChatCustomerSearch.code () != null) {

				criteria.add (
					Restrictions.eq (
						"_imChatCustomer.code",
						imChatCustomerSearch.code ()));

			}

			if (
				isNotNull (
					imChatCustomerSearch.email ())
			) {

				criteria.add (
					Restrictions.eq (
						"_imChatCustomer.email",
						imChatCustomerSearch.email ()));

			}

			if (
				isNotNull (
					imChatCustomerSearch.firstSession ())
			) {

				criteria.add (
					Restrictions.ge (
						"_imChatCustomer.firstSession",
						imChatCustomerSearch.firstSession ().start ()));

				criteria.add (
					Restrictions.lt (
						"_imChatCustomer.firstSession",
						imChatCustomerSearch.firstSession ().end ()));

			}

			if (
				isNotNull (
					imChatCustomerSearch.lastSession ())
			) {

				criteria.add (
					Restrictions.ge (
						"_imChatCustomer.lastSession",
						toInstant (
							imChatCustomerSearch.lastSession ().start ())));

				criteria.add (
					Restrictions.lt (
						"_imChatCustomer.lastSession",
						toInstant (
							imChatCustomerSearch.lastSession ().end ())));

			}

			// set order

			switch (imChatCustomerSearch.order ()) {

			case timestampDesc:

				criteria

					.addOrder (
						Order.desc (
							"_imChatCustomer.lastSession"));

				break;

			case totalPurchaseDesc:

				criteria

					.addOrder (
						Order.desc (
							"_imChatCustomer.totalPurchase"));

				break;

			case balanceDesc:

				criteria

					.addOrder (
						Order.desc (
							"_imChatCustomer.balance"));

				break;

			default:

				throw new RuntimeException (
					"should never happen");

			}

			// set to return ids only

			criteria

				.setProjection (
					Projections.id ());

			// perform and return

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
