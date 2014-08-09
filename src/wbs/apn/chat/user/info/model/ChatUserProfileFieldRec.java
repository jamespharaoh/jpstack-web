package wbs.apn.chat.user.info.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatUserProfileFieldRec
	implements EphemeralRecord<ChatUserProfileFieldRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatUserRec chatUser;

	@IdentityReferenceField
	ChatProfileFieldRec chatProfileField;

	@ReferenceField
	ChatProfileFieldValueRec chatProfileFieldValue;

	@Override
	public
	int compareTo (
			Record<ChatUserProfileFieldRec> otherRecord) {

		ChatUserProfileFieldRec other =
			(ChatUserProfileFieldRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatUser (),
				other.getChatUser ())

			.append (
				getChatProfileField (),
				other.getChatProfileField ())

			.toComparison ();

	}

}