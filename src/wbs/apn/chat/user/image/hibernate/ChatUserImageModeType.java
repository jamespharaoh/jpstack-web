package wbs.apn.chat.user.image.hibernate;

import java.sql.Types;

import wbs.apn.chat.user.image.model.ChatUserImageMode;
import wbs.framework.hibernate.EnumUserType;

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