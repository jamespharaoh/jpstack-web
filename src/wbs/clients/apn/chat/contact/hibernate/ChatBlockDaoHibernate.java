package wbs.clients.apn.chat.contact.hibernate;

import wbs.clients.apn.chat.contact.model.ChatBlockDao;
import wbs.clients.apn.chat.contact.model.ChatBlockRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatBlockDaoHibernate
	extends HibernateDao
	implements ChatBlockDao {

	@Override
	public
	ChatBlockRec find (
			ChatUserRec chatUser,
			ChatUserRec blockedChatUser) {

		return findOne (
			ChatBlockRec.class,

			createQuery (
				"FROM ChatBlockRec chatBlock " +
				"WHERE chatBlock.chatUser = :chatUser " +
					"AND chatBlock.blockedChatUser = :blockedChatUser")

			.setEntity (
				"chatUser",
				chatUser)

			.setEntity (
				"blockedChatUser",
				blockedChatUser)

			.list ());

	}

}
