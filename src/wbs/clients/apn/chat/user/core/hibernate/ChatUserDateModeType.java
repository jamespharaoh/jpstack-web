package wbs.clients.apn.chat.user.core.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserDateModeType
	extends EnumUserType<Integer,ChatUserDateMode> {

	{

		sqlType (Types.INTEGER);
		enumClass (ChatUserDateMode.class);

		add (-1, ChatUserDateMode.none);
		add (0, ChatUserDateMode.text);
		add (1, ChatUserDateMode.photo);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new ChatUserDateModeType ());

}
