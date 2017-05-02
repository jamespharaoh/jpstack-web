package wbs.apn.chat.bill.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.bill.model.ChatUserCreditMode;

public
class ChatUserCreditModeType
	extends EnumUserType <Long, ChatUserCreditMode> {

	{

		sqlType (Types.BIGINT);

		enumClass (ChatUserCreditMode.class);

		add (1l, ChatUserCreditMode.billedMessages);
		add (2l, ChatUserCreditMode.prePay);
		add (3l, ChatUserCreditMode.barred);
		add (4l, ChatUserCreditMode.free);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new ChatUserCreditModeType ());

}
