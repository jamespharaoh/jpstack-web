package wbs.apn.chat.contact.hibernate;

import org.hibernate.FlushMode;

import wbs.apn.chat.contact.model.ChatContactDao;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatContactDaoHibernate
	extends HibernateDao
	implements ChatContactDao {

	@Override
	public
	ChatContactRec find (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser) {

		return findOne (
			ChatContactRec.class,

			createQuery (
				"FROM ChatContactRec chatContact " +
				"WHERE chatContact.fromUser = :fromChatUser " +
					"AND chatContact.toUser = :toChatUser")

			.setEntity (
				"fromChatUser",
				fromChatUser)

			.setEntity (
				"toChatUser",
				toChatUser)

			.setFlushMode (
				FlushMode.MANUAL)

			.list ());

	}

}
