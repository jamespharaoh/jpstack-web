package wbs.apn.chat.user.image.model;

import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;

public
interface ChatUserImageUploadTokenDaoMethods {

	ChatUserImageUploadTokenRec findByToken (
			String token);

}