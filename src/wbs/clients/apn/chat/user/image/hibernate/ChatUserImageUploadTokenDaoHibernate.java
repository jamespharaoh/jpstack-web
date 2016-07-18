package wbs.clients.apn.chat.user.image.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenDao;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserImageUploadTokenDaoHibernate
	extends HibernateDao
	implements ChatUserImageUploadTokenDao {

	@Override
	public
	ChatUserImageUploadTokenRec findByToken (
			@NonNull String token) {

		return findOne (
			"findByToken (token)",
			ChatUserImageUploadTokenRec.class,

			createCriteria (
				ChatUserImageUploadTokenRec.class)

			.add (
				Restrictions.eq (
					"token",
					token))

		);

	}

}
