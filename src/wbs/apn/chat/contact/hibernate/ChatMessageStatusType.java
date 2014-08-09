package wbs.apn.chat.contact.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.framework.hibernate.EnumUserType;

public
class ChatMessageStatusType
	extends EnumUserType<Integer,ChatMessageStatus> {

	{

		sqlType (Types.INTEGER);
		enumClass (ChatMessageStatus.class);

		add (0, ChatMessageStatus.sent);
		add (1, ChatMessageStatus.blocked);
		add (2, ChatMessageStatus.autoEdited);
		add (3, ChatMessageStatus.moderatorPending);
		add (4, ChatMessageStatus.moderatorApproved);
		add (5, ChatMessageStatus.moderatorRejected);
		add (6, ChatMessageStatus.moderatorAutoEdited);
		add (7, ChatMessageStatus.moderatorEdited);
		add (8, ChatMessageStatus.signup);
		add (9, ChatMessageStatus.signupReplaced);
		add (10, ChatMessageStatus.signupTimeout);

	}

	public final
	static Type INSTANCE =
		new CustomType (
			new ChatMessageStatusType ());

}
