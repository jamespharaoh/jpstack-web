package wbs.apn.chat.user.image.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.user.image.model.ChatUserImageMode;

public
class ChatUserImageModeType
	extends EnumUserType<String,ChatUserImageMode> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatUserImageMode.class);

		add ("link", ChatUserImageMode.link);
		add ("mms", ChatUserImageMode.mms);

	}

}