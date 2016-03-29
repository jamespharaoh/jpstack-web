package wbs.clients.apn.chat.contact.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogDao;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogSearch;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserInitiationLogDaoHibernate
	extends HibernateDao
	implements ChatUserInitiationLogDao {

	@Override
	public
	List<ChatUserInitiationLogRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval) {

		return findMany (
			ChatUserInitiationLogRec.class,

			createQuery (
				"FROM ChatUserInitiationLogRec log " +
				"WHERE log.chatUser.chat = :chat " +
					"AND log.timestamp >= :startTime " +
					"AND log.timestamp < :endTime")

			.setEntity (
				"chat",
				chat)

			.setTimestamp (
				"startTime",
				instantToDate (
					timestampInterval.getStart ()))

			.setDate (
				"endTime",
				instantToDate (
					timestampInterval.getEnd ()))

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
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
					instantToDate (
						search.timestamp ().start ())));

			criteria.add (
				Restrictions.lt (
					"_chatUserInitiationLog.timestamp",
					instantToDate (
						search.timestamp ().end ())));

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

			List<Criterion> filterCriteria =
				new ArrayList<Criterion> ();

			if (
				isNotEmpty (
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
			Integer.class,
			criteria.list ());

	}

}
