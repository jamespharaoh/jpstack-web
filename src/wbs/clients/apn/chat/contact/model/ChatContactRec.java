package wbs.clients.apn.chat.contact.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatContactRec
	implements CommonRecord<ChatContactRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@IdentityReferenceField
	ChatUserRec fromUser;

	@IdentityReferenceField
	ChatUserRec toUser;

	// details

	@ReferenceField (
		nullable = true)
	ChatContactRec inverseChatContact;

	// statistics

	@SimpleField (
		nullable = true)
	Date firstMessageTime;

	@SimpleField (
		nullable = true)
	Date lastMessageTime;

	@SimpleField (
		nullable = true,
		column = "last_message")
	Date lastDeliveredMessageTime;

	@SimpleField (
		nullable = true,
		column = "last_info")
	Date lastInfoTime;

	@SimpleField (
		nullable = true,
		column = "last_pic")
	Date lastPicTime;

	@SimpleField (
		nullable = true,
		column = "last_video")
	Date lastVideoTime;

	@SimpleField (
		nullable = true)
	Integer numChatMessages = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatContactRec> otherRecord) {

		ChatContactRec other =
			(ChatContactRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getFromUser (),
				other.getFromUser ())

			.append (
				getToUser (),
				other.getToUser ())

			.toComparison ();

	}

}
