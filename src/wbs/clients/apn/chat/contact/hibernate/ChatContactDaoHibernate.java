package wbs.clients.apn.chat.contact.hibernate;

import lombok.NonNull;

import org.hibernate.FlushMode;
import org.hibernate.criterion.Restrictions;

import wbs.clients.apn.chat.contact.model.ChatContactDao;
import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatContactDaoHibernate
	extends HibernateDao
	implements ChatContactDao {

	@Override
	public
	ChatContactRec findNoFlush (
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser) {

		return findOne (
			"findOne (fromChatUser, toChatUser)",
			ChatContactRec.class,

			createCriteria(
				ChatContactRec.class,
				"_chatContact")

			.add (
				Restrictions.eq (
					"_chatContact.fromUser",
					fromChatUser))

			.add (
				Restrictions.eq (
					"_chatContact.toUser",
					toChatUser))

			.setFlushMode (
				FlushMode.MANUAL)

		);

	}

}
