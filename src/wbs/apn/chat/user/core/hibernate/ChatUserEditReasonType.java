package wbs.apn.chat.user.core.hibernate;

import java.sql.Types;

import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserEditReasonType
	extends EnumUserType<Integer,ChatUserEditReason> {

	{

		sqlType (Types.INTEGER);
		enumClass (ChatUserEditReason.class);

		add (0, ChatUserEditReason.other);
		add (1, ChatUserEditReason.userRequest);
		add (2, ChatUserEditReason.inappropriateContent);

	}

}
