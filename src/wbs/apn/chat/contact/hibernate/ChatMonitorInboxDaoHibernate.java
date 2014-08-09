package wbs.apn.chat.contact.hibernate;

import wbs.apn.chat.contact.model.ChatMonitorInboxDao;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatMonitorInboxDaoHibernate
	extends HibernateDao
	implements ChatMonitorInboxDao {

	@Override
	public
	ChatMonitorInboxRec find (
			ChatUserRec monitorChatUser,
			ChatUserRec userChatUser) {

		return findOne (
			ChatMonitorInboxRec.class,

			createQuery (
				"FROM ChatMonitorInboxRec cmi " +
				"WHERE cmi.monitorChatUser = :monitorChatUser " +
					"AND cmi.userChatUser = :userChatUser")

			.setEntity (
				"monitorChatUser",
				monitorChatUser)

			.setEntity (
				"userChatUser",
				userChatUser)

			.list ());

	}

}
