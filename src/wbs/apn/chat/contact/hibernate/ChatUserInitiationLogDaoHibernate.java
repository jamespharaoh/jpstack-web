package wbs.apn.chat.contact.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.isNotNull;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import lombok.NonNull;

import wbs.apn.chat.contact.model.ChatUserInitiationLogSearch;
import wbs.apn.chat.contact.model.ChatUserInitiationLogDao;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserInitiationLogDaoHibernate
	extends HibernateDao
	implements ChatUserInitiationLogDao {

	@Override
	public
	List<ChatUserInitiationLogRec> findByTimestamp (
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		return findMany (
			"findByTimestamp (chat, timestamp)",
			ChatUserInitiationLogRec.class,

			createCriteria (
				ChatUserInitiationLogRec.class,
				"_chatUserInitiationLog")

			.createAlias (
				"_chatUserInitiationLog.chatUser",
				"_chatUser")

			.add (
				Restrictions.eq (
					"_chatUser.chat",
					chat))

			.add (
				Restrictions.ge (
					"_chatUserInitiationLog.timestamp",
					timestamp.getStart ()))

			.add (
				Restrictions.lt (
					"_chatUserInitiationLog.timestamp",
					timestamp.getEnd ()))

		);

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull ChatUserInitiationLogSearch search) {

		Criteria criteria =
			createCriteria (
				ChatUserInitiationLogRec.class,
				"_chatUserInitiationLog")

			.createAlias (
				"_chatUserInitiationLog.chatUser",
				"_chatUser")

			.createAlias (
				"_chatUser.chat",
				"_chat");

		if (
			isNotNull (
				search.chatId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUserInitiationLog.chat.id",
					search.chatId ()));

		}

		if (
			isNotNull (
				search.timestamp ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUserInitiationLog.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatUserInitiationLog.timestamp",
					search.timestamp ().end ()));

		}

		if (
			isNotNull (
				search.reason ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUserInitiationLog.reason",
					search.reason ()));

		}

		if (
			isNotNull (
				search.monitorUserId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_chatUserInitiationLog.monitorUser.id",
					search.monitorUserId ()));

		}

		if (search.filter ()) {

			List <Criterion> filterCriteria =
				new ArrayList<> ();

			if (
				collectionIsNotEmpty (
					search.filterChatIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"_chat.id",
						search.filterChatIds ()));

			}

			criteria.add (
				Restrictions.or (
					filterCriteria.toArray (
						new Criterion [] {})));

		}

		criteria.addOrder (
			Order.desc (
				"_chatUserInitiationLog.timestamp"));

		criteria.setProjection (
			Projections.id ());

		return findMany (
			"search (search)",
			Long.class,
			criteria);

	}

}
