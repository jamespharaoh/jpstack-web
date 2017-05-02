package wbs.apn.chat.user.core.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.user.core.model.ChatUserEditReason;

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
