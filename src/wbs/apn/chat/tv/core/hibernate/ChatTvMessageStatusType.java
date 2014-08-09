package wbs.apn.chat.tv.core.hibernate;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.apn.chat.tv.core.model.ChatTvMessageStatus;
import wbs.framework.hibernate.EnumUserType;

public
class ChatTvMessageStatusType
	extends EnumUserType<String,ChatTvMessageStatus> {

	{

		sqlType (1111);
		enumClass (ChatTvMessageStatus.class);

		add ("signup", ChatTvMessageStatus.signup);
		add ("replaced", ChatTvMessageStatus.replaced);
		add ("timeout", ChatTvMessageStatus.timeout);
		add ("moderating", ChatTvMessageStatus.moderating);
		add ("rejected", ChatTvMessageStatus.rejected);
		add ("holding", ChatTvMessageStatus.holding);
		add ("sending", ChatTvMessageStatus.sending);
		add ("sent", ChatTvMessageStatus.sent);
		add ("approved", ChatTvMessageStatus.approved);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new ChatTvMessageStatusType ());

}
