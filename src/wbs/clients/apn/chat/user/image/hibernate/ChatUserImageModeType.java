package wbs.clients.apn.chat.user.image.hibernate;

import java.sql.Types;

import wbs.clients.apn.chat.user.image.model.ChatUserImageMode;
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