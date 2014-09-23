package wbs.apn.chat.bill.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserCreditModeType
	extends EnumUserType<Integer,ChatUserCreditMode> {

	{

		sqlType (Types.INTEGER);
		enumClass (ChatUserCreditMode.class);

		add (1, ChatUserCreditMode.strict);
		add (2, ChatUserCreditMode.prePay);
		add (3, ChatUserCreditMode.barred);
		add (4, ChatUserCreditMode.free);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new ChatUserCreditModeType ());

}
