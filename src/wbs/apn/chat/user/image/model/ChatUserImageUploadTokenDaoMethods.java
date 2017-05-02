package wbs.apn.chat.user.image.model;

import wbs.framework.database.Transaction;

public
interface ChatUserImageUploadTokenDaoMethods {

	ChatUserImageUploadTokenRec findByToken (
			Transaction parentTransaction,
			String token);

}