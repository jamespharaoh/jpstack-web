package wbs.apn.chat.contact.hibernate;

import java.sql.Types;

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.framework.hibernate.EnumUserType;

public
class ChatMessageMethodType
	extends EnumUserType<String,ChatMessageMethod> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatMessageMethod.class);

		add ("sms", ChatMessageMethod.sms);
		add ("iphone", ChatMessageMethod.iphone);
		add ("web", ChatMessageMethod.web);
		add ("api", ChatMessageMethod.api);
		add ("info-site", ChatMessageMethod.infoSite);

	}

}