package wbs.apn.chat.user.core.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.user.core.model.ChatUserType;

public
class ChatUserTypeType
	extends EnumUserType<String,ChatUserType> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatUserType.class);

		add ("u", ChatUserType.user);
		add ("m", ChatUserType.monitor);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new ChatUserTypeType ());

}
