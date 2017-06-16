package wbs.imchat.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatMessageDao;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatMessageSearch;
import wbs.imchat.model.ImChatMessageUserStats;
import wbs.imchat.model.ImChatMessageViewRec;
import wbs.imchat.model.ImChatOperatorReport;

public
class ImChatMessageDaoHibernate
	extends HibernateDao
	implements ImChatMessageDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Criteria searchCriteria (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchCriteria");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ImChatMessageRec.class,
					"_imChatMessage")

				.createAlias (
					"_imChatMessage.imChatConversation",
					"_imChatConversation")

				.createAlias (
					"_imChatConversation.imChatCustomer",
					"_imChatCustomer")

				.createAlias (
					"_imChatCustomer.imChat",
					"_imChat")

				.createAlias (
					"_imChatMessage.senderUser",
					"_senderUser")

				.createAlias (
					"_senderUser.slice",
					"_senderUserSlice");

			if (
				isNotNull (
					search.imChatIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_imChat.id",
						search.imChatIds ()));

			}

			if (
				isNotNull (
					search.senderUserIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_senderUser.id",
						search.senderUserIds ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_imChatMessage.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_imChatMessage.timestamp",
						search.timestamp ().end ()));

			}

			return criteria;

		}

	}

	@Override
	public
	Criteria searchOperatorReportCriteria (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchOperatorReportCriteria");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.property (
						"_imChatMessage.senderUser"),
					"user")

				.add (
					Projections.sqlProjection (
						stringFormat (
							"sum (CASE WHEN {alias}.price IS NULL THEN 1 ELSE 0 ",
							"END) AS num_free_messages"),
						new String [] {
							"num_free_messages",
						},
						new Type [] {
							LongType.INSTANCE,
						}),
					"numFreeMessages")

				.add (
					Projections.sqlProjection (
						stringFormat (
							"sum (CASE WHEN {alias}.price IS NOT NULL THEN 1 ELSE ",
							"0 END) AS num_billed_messages"),
						new String [] {
							"num_billed_messages",
						},
						new Type [] {
							LongType.INSTANCE,
						}),
					"numBilledMessages")

				.add (
					Projections.groupProperty (
						"_imChatMessage.senderUser"))

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ImChatOperatorReport.class));

			return criteria;

		}

	}

	@Override
	public
	List <Long> searchOperatorReportIds (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchOperatorReportIds");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.distinct (
						Projections.property (
							"_senderUser.id")))

				.add (
					Projections.groupProperty (
						"_senderUser.id"))

				.add (
					Projections.groupProperty (
						"_senderUserSlice.code"))

				.add (
					Projections.groupProperty (
						"_senderUser.username"))

			);

			criteria.addOrder (
				Order.asc (
					"_senderUserSlice.code"));

			criteria.addOrder (
				Order.asc (
					"_senderUser.username"));

			return findIdsOnly (
				criteria.list ());

		}

	}

	@Override
	public
	List <Optional <ImChatOperatorReport>> findOperatorReports (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageSearch search,
			@NonNull List <Long> ids) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOperatorReports");

		) {

			Criteria criteria =
				searchOperatorReportCriteria (
					transaction,
					search);

			criteria.add (
				Restrictions.in (
					"_senderUser.id",
					ids));

			return findOrdered (
				transaction,
				ImChatOperatorReport.class,
				ids,
				criteria.list ());

		}

	}

	@Override
	public
	List <ImChatMessageUserStats> searchMessageUserStats (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatMessageSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchMessageUserStats");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ImChatMessageViewRec.class,
					"_imChatMessageView")

				.createAlias (
					"_imChatMessageView.imChat",
					"_imChat")

				.createAlias (
					"_imChatMessageView.senderUser",
					"_senderUser")

			;

			if (
				isNotNull (
					search.imChatIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_imChat.id",
						search.imChatIds ()));

			}

			if (
				isNotNull (
					search.senderUserIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_senderUser.id",
						search.senderUserIds ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_imChatMessageView.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_imChatMessageView.timestamp",
						search.timestamp ().end ()));

			}

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.groupProperty (
						"_imChatMessageView.imChat"),
					"imChat")

				.add (
					Projections.groupProperty (
						"_imChatMessageView.senderUser"),
					"senderUser")

				.add (
					Projections.rowCount (),
					"numMessages")

				.add (
					Projections.sum (
						"_imChatMessageView.numCharacters"),
					"numCharacters")

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ImChatMessageUserStats.class));

			return findMany (
				transaction,
				ImChatMessageUserStats.class,
				criteria);

		}

	}

}
