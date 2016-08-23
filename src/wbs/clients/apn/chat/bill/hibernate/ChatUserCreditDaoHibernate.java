package wbs.clients.apn.chat.bill.hibernate;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import lombok.NonNull;
import wbs.clients.apn.chat.bill.model.ChatUserCreditDao;
import wbs.clients.apn.chat.bill.model.ChatUserCreditRec;
import wbs.clients.apn.chat.bill.model.ChatUserCreditSearch;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserCreditDaoHibernate
	extends HibernateDao
	implements ChatUserCreditDao {

	@Override
	public
	List<ChatUserCreditRec> findByTimestamp (
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		return findMany (
			"findByTimestamp (chat, timestamp)",
			ChatUserCreditRec.class,

			createCriteria (
				ChatUserCreditRec.class,
				"_chatUserCredit")

			.createAlias (
				"_chatUserCredit.chatUser",
				"_chatUser")

			.add (
				Restrictions.eq (
					"_chatUser.chat",
					chat))

			.add (
				Restrictions.ge (
					"_chatUserCredit.timestamp",
					timestamp.getStart ()))

			.add (
				Restrictions.lt (
					"_chatUserCredit.timestamp",
					timestamp.getEnd ()))

		);

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull ChatUserCreditSearch search) {

		Criteria criteria =

			createCriteria (
				ChatUserCreditRec.class,
				"_chatUserCredit")

			.createAlias (
				"_chatUserCredit.chatUser",
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
					"_chat.id",
					search.chatId ()));

		}

		if (
			isNotNull (
				search.timestamp ())
		) {

			criteria.add (
				Restrictions.ge (
					"_chatUserCredit.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_chatUserCredit.timestamp",
					search.timestamp ().end ()));

		}

		if (search.filter ()) {

			criteria.add (
				Restrictions.or (
					Restrictions.in (
						"_chat.id",
						search.filterChatIds ())));

		}

		criteria.addOrder (
			Order.desc (
				"_chatUserCredit.timestamp"));

		criteria.addOrder (
			Order.desc (
				"_chatUserCredit.id"));

		criteria.setProjection (
			Projections.id ());

		return findMany (
			"search (search)",
			Long.class,
			criteria);

	}

}
