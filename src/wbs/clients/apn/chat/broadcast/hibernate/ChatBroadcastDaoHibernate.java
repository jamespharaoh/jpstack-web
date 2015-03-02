package wbs.clients.apn.chat.broadcast.hibernate;

import java.util.List;

import wbs.clients.apn.chat.broadcast.model.ChatBroadcastDao;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
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
			ChatRec chat,
			int firstResult,
			int maxResults) {

		return findMany (
			ChatBroadcastRec.class,

			createQuery (
				"FROM ChatBroadcastRec broadcast " +
				"WHERE broadcast.chat = :chat " +
				"ORDER BY broadcast.timestamp DESC")

			.setEntity (
				"chat",
				chat)

			.setFirstResult (
				firstResult)

			.setMaxResults (
				maxResults)

			.list ());

	}

}
