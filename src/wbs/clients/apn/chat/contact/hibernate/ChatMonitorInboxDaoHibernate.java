package wbs.clients.apn.chat.contact.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.clients.apn.chat.contact.model.ChatMonitorInboxDao;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatMonitorInboxDaoHibernate
	extends HibernateDao
	implements ChatMonitorInboxDao {

	@Override
	public
	ChatMonitorInboxRec find (
			@NonNull ChatUserRec monitorChatUser,
			@NonNull ChatUserRec userChatUser) {

		return findOne (
			"find (monitorChatUser, userChatUser)",
			ChatMonitorInboxRec.class,

			createCriteria (
				ChatMonitorInboxRec.class,
				"_chatMonitorInbox")

			.add (
				Restrictions.eq (
					"_chatMonitorInbox.monitorChatUser",
					monitorChatUser))

			.add (
				Restrictions.eq (
					"_chatMonitorInbox.userChatUser",
					userChatUser))

		);

	}

}
