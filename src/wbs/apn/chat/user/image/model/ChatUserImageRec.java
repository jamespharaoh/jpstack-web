package wbs.apn.chat.user.image.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserImageRec
	implements CommonRecord<ChatUserImageRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	MediaRec media;

	@ReferenceField
	MediaRec fullMedia;

	@SimpleField
	Date timestamp;

	@ReferenceField (
		nullable = true)
	MessageRec message;

	@SimpleField
	ChatUserInfoStatus status;

	@ReferenceField (
		nullable = true,
		column = "user_id")
	UserRec moderator;

	@SimpleField  (
		nullable = true)
	Date moderationTime;

	@SimpleField (
		nullable = true)
	Integer threadId;

	@SimpleField
	ChatUserImageType type;

	@SimpleField (
		nullable = true)
	Integer index;

	@SimpleField (
		nullable = true)
	Boolean append;

	@SimpleField
	String classification =
		"unknown"; // TODO enum

	@Override
	public
	int compareTo (
			Record<ChatUserImageRec> otherRecord) {

		ChatUserImageRec other =
			(ChatUserImageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatUser (),
				other.getChatUser ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
