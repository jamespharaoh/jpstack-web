package wbs.clients.apn.chat.bill.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserCreditModeType
	extends EnumUserType <Long, ChatUserCreditMode> {

	{

		sqlType (Types.BIGINT);

		enumClass (ChatUserCreditMode.class);

		add (1l, ChatUserCreditMode.strict);
		add (2l, ChatUserCreditMode.prePay);
		add (3l, ChatUserCreditMode.barred);
		add (4l, ChatUserCreditMode.free);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new ChatUserCreditModeType ());

}
