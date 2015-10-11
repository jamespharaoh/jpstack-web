package wbs.clients.apn.chat.user.image.model;

public
interface ChatUserImageUploadTokenDaoMethods {

	ChatUserImageUploadTokenRec findByToken (
			String token);

}