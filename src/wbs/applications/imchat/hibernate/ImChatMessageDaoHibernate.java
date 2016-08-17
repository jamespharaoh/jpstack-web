package wbs.applications.imchat.hibernate;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import lombok.NonNull;
import wbs.applications.imchat.model.ImChatMessageDao;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatMessageSearch;
import wbs.applications.imchat.model.ImChatOperatorReport;
import wbs.framework.hibernate.HibernateDao;

public
class ImChatMessageDaoHibernate
	extends HibernateDao
	implements ImChatMessageDao {

	@Override
	public
	Criteria searchCriteria (
			@NonNull ImChatMessageSearch search) {

		Criteria criteria =
			createCriteria (
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
				search.imChatId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_imChat.id",
					search.imChatId ()));

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

	@Override
	public
	Criteria searchOperatorReportCriteria (
			@NonNull ImChatMessageSearch search) {

		Criteria criteria =
			searchCriteria (
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

	@Override
	public
	List <Long> searchOperatorReportIds (
			@NonNull ImChatMessageSearch search) {

		Criteria criteria =
			searchCriteria (
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

	@Override
	public
	List<ImChatOperatorReport> findOperatorReports (
			@NonNull ImChatMessageSearch search,
			@NonNull List<Long> ids) {

		Criteria criteria =
			searchOperatorReportCriteria (
				search);

		criteria.add (
			Restrictions.in (
				"_senderUser.id",
				ids));

		return findOrdered (
			ImChatOperatorReport.class,
			ids,
			criteria.list ());

	}

}
