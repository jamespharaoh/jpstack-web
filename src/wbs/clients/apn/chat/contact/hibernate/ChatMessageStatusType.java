package wbs.clients.apn.chat.contact.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.framework.hibernate.EnumUserType;

public
class ChatMessageStatusType
	extends EnumUserType <Long, ChatMessageStatus> {

	{

		sqlType (Types.BIGINT);
		enumClass (ChatMessageStatus.class);

		add (0l, ChatMessageStatus.sent);
		add (1l, ChatMessageStatus.blocked);
		add (2l, ChatMessageStatus.autoEdited);
		add (3l, ChatMessageStatus.moderatorPending);
		add (4l, ChatMessageStatus.moderatorApproved);
		add (5l, ChatMessageStatus.moderatorRejected);
		add (6l, ChatMessageStatus.moderatorAutoEdited);
		add (7l, ChatMessageStatus.moderatorEdited);
		add (8l, ChatMessageStatus.signup);
		add (9l, ChatMessageStatus.signupReplaced);
		add (10l, ChatMessageStatus.signupTimeout);
		add (11l, ChatMessageStatus.broadcast);

	}

	public final
	static Type INSTANCE =
		new CustomType (
			new ChatMessageStatusType ());

}
