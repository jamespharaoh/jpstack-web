package wbs.apn.chat.contact.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.contact.model.ChatBlockDao;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatBlockDaoHibernate
	extends HibernateDao
	implements ChatBlockDao {

	@Override
	public
	ChatBlockRec find (
			@NonNull ChatUserRec chatUser,
			@NonNull ChatUserRec blockedChatUser) {

		return findOne (
			"find (chatUser, blockedChatUser)",
			ChatBlockRec.class,

			createCriteria (
				ChatBlockRec.class,
				"_chatBlock")

			.add (
				Restrictions.eq (
					"_chatBlock.chatUser",
					chatUser))

			.add (
				Restrictions.eq (
					"_chatBlock.blockedChatUser",
					blockedChatUser))

		);

	}

}
