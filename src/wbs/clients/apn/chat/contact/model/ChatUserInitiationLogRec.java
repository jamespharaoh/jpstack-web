package wbs.clients.apn.chat.contact.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class ChatUserInitiationLogRec
	implements CommonRecord<ChatUserInitiationLogRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	ChatUserRec monitorChatUser;

	// TODO contact, index

	// details

	@ReferenceField
	UserRec monitorUser;

	@SimpleField
	Date timestamp;

	@SimpleField (
		nullable = true)
	Date alarmTime;

	@SimpleField
	ChatUserInitiationReason reason;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserInitiationLogRec> otherRecord) {

		ChatUserInitiationLogRec other =
			(ChatUserInitiationLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
