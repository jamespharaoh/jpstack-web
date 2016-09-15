package wbs.apn.chat.user.info.hibernate;

import java.sql.Types;

import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserInfoStatusType
	extends EnumUserType<Integer,ChatUserInfoStatus> {

	{

		sqlType (Types.INTEGER);
		enumClass (ChatUserInfoStatus.class);

		add (0, ChatUserInfoStatus.set);
		add (1, ChatUserInfoStatus.autoEdited);
		add (2, ChatUserInfoStatus.moderatorPending);
		add (3, ChatUserInfoStatus.moderatorApproved);
		add (4, ChatUserInfoStatus.moderatorRejected);
		add (5, ChatUserInfoStatus.moderatorAutoEdited);
		add (6, ChatUserInfoStatus.moderatorEdited);
		add (7, ChatUserInfoStatus.console);

	}

}
