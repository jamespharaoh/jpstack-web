package wbs.clients.apn.chat.user.image.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
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
class ChatUserImageUploadTokenRec
	implements CommonRecord<ChatUserImageUploadTokenRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec chatUser;

	@IndexField
	Integer index;

	// details

	@SimpleField
	String token;

	@ReferenceField (
		nullable = true)
	MessageRec messageIn;

	@ReferenceField (
		nullable = true)
	UserRec user;

	@ReferenceField
	MessageRec messageOut;

	@SimpleField
	Instant createdTime;

	@SimpleField
	Instant expiryTime;

	// statistics

	@SimpleField (
		nullable = true)
	Instant firstViewTime;

	@SimpleField (
		nullable = true)
	Instant lastViewTime;

	@SimpleField (
		nullable = true)
	Instant firstExpiredTime;

	@SimpleField (
		nullable = true)
	Instant lastExpiredTime;

	@SimpleField (
		nullable = true)
	Instant firstUploadTime;

	@SimpleField (
		nullable = true)
	Instant lastUploadTime;

	@SimpleField (
		nullable = true)
	Instant firstFailedTime;

	@SimpleField (
		nullable = true)
	Instant lastFailedTime;

	@SimpleField
	Integer numViews = 0;

	@SimpleField
	Integer numExpired = 0;

	@SimpleField
	Integer numUploads = 0;

	@SimpleField
	Integer numFailures = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserImageUploadTokenRec> otherRecord) {

		ChatUserImageUploadTokenRec other =
			(ChatUserImageUploadTokenRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
