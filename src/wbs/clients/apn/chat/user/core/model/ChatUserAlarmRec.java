package wbs.clients.apn.chat.user.core.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatUserAlarmRec
	implements EphemeralRecord<ChatUserAlarmRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	ChatUserRec monitorChatUser;

	@SimpleField
	Date alarmTime;

	@SimpleField
	Date resetTime;

	@SimpleField
	Boolean sticky;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserAlarmRec> otherRecord) {

		ChatUserAlarmRec other =
			(ChatUserAlarmRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getAlarmTime (),
				getAlarmTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
