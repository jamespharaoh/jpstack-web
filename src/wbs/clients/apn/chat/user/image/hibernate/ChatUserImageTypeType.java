package wbs.clients.apn.chat.user.image.hibernate;

import java.sql.Types;

import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserImageTypeType
	extends EnumUserType<String,ChatUserImageType> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatUserImageType.class);

		add ("image", ChatUserImageType.image);
		add ("video", ChatUserImageType.video);
		add ("audio", ChatUserImageType.audio);

	}

}
