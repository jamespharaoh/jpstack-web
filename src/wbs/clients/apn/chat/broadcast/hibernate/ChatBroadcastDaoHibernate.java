package wbs.clients.apn.chat.broadcast.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.clients.apn.chat.broadcast.model.ChatBroadcastDao;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

@SingletonComponent ("chatBroadcastDao")
public
class ChatBroadcastDaoHibernate
	extends HibernateDao
	implements ChatBroadcastDao {

	@Override
	public
	List<ChatBroadcastRec> findRecentWindow (
			@NonNull ChatRec chat,
			int firstResult,
			int maxResults) {

		return findMany (
			"findManyWindow (chat, firstResult, maxResults)",
			ChatBroadcastRec.class,

			createCriteria (
				ChatBroadcastRec.class,
				"_chatBroadcast")

			.add (
				Restrictions.eq (
					"_chatBroadcast.chat",
					chat))

			.addOrder (
				Order.desc (
					"_chatBroadcast.createdTime"))

			.setFirstResult (
				firstResult)

			.setMaxResults (
				maxResults)

		);

	}

	@Override
	public
	List<ChatBroadcastRec> findSending () {

		return findMany (
			"findSending",
			ChatBroadcastRec.class,

			createCriteria (
				ChatBroadcastRec.class,
				"_chatBroadcast")

			.add (
				Restrictions.eq (
					"_chatBroadcast.state",
					ChatBroadcastState.sending))

		);

	}

	@Override
	public
	List<ChatBroadcastRec> findScheduled (
			@NonNull Instant scheduledTime) {

		return findMany (
			"findScheduled (scheduledTime)",
			ChatBroadcastRec.class,

			createCriteria (
				ChatBroadcastRec.class,
				"_chatBroadcast")

			.add (
				Restrictions.eq (
					"_chatBroadcast.state",
					ChatBroadcastState.scheduled))

			.add (
				Restrictions.le (
					"_chatBroadcast.scheduledTime",
					scheduledTime))

		);

	}

}
