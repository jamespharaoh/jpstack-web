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
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserNameRec
	implements CommonRecord<ChatUserNameRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec chatUser;

	// TODO index

	// details

	@SimpleField (
		nullable = true,
		column = "name")
	String originalName;

	@SimpleField
	String editedName;

	@SimpleField (
		column = "timestamp")
	Date creationTime;

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
			Record<ChatUserNameRec> otherRecord) {

		ChatUserNameRec other =
			(ChatUserNameRec) otherRecord;

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
