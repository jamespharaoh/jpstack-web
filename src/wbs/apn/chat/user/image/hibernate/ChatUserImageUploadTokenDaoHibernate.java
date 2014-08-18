package wbs.apn.chat.user.image.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenDao;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserImageUploadTokenDaoHibernate
	extends HibernateDao
	implements ChatUserImageUploadTokenDao {

	@Override
	public
	ChatUserImageUploadTokenRec findByToken (
			String token) {

		return findOne (
			ChatUserImageUploadTokenRec.class,

			createCriteria (
				ChatUserImageUploadTokenRec.class)

			.add (
				Restrictions.eq (
					"token",
					token))

			.list ());

	}

}
