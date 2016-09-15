package wbs.apn.chat.contact.hibernate;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.FlushMode;
import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.contact.model.ChatContactDao;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatContactDaoHibernate
	extends HibernateDao
	implements ChatContactDao {

	// implementation

	@Override
	public
	Optional <ChatContactRec> findNoCache (
			@NonNull ChatUserRec fromChatUser,
			@NonNull ChatUserRec toChatUser) {

		return optionalFromNullable (
			findOne (
				"find (fromChatUser, toChatUser)",
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

		));

	}

}
