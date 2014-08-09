package wbs.apn.chat.user.info.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserInfoRec
	implements CommonRecord<ChatUserInfoRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ReferenceField
	ChatUserRec chatUser;

	// TODO index

	// details

	@SimpleField (
		column = "timestamp")
	Date creationTime;

	@ReferenceField (
		nullable = true,
		column = "text_id")
	TextRec originalText;

	@ReferenceField (
		nullable = true)
	TextRec editedText;

	@ReferenceField (
		nullable = true,
		column = "user_id")
	UserRec moderator;

	@SimpleField
	ChatUserInfoStatus status;

	@SimpleField (
		nullable = true)
	Date moderationTime;

	@SimpleField (
		nullable = true)
	Integer threadId;

	@SimpleField (
		nullable = true)
	ChatUserEditReason editReason;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserInfoRec> otherRecord) {

		ChatUserInfoRec other =
			(ChatUserInfoRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreationTime (),
				getCreationTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
