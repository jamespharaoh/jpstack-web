package wbs.apn.chat.user.image.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.user.image.model.ChatUserImageType;

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
