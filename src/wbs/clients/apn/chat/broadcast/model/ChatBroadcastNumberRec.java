package wbs.clients.apn.chat.broadcast.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatBroadcastNumberRec
	implements CommonRecord<ChatBroadcastNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatBroadcastRec chatBroadcast;

	@IdentityReferenceField
	ChatUserRec chatUser;

	// state

	@SimpleField
	ChatBroadcastNumberState state;

	// other information

	@ReferenceField (
		nullable = true)
	UserRec addedByUser;

	@ReferenceField (
		nullable = true)
	UserRec removedByUser;

	@ReferenceField (
		nullable = true)
	MessageRec message;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatBroadcastNumberRec> otherRecord) {

		ChatBroadcastNumberRec other =
			(ChatBroadcastNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatBroadcast (),
				other.getChatBroadcast ())

			.append (
				getChatUser (),
				other.getChatUser ())

			.toComparison ();

	}

	// dao

	public static
	interface ChatBroadcastNumberDaoMethods {

		List<ChatBroadcastNumberRec> findAcceptedLimit (
				ChatBroadcastRec chatBroadcast,
				int limit);

	}

}
