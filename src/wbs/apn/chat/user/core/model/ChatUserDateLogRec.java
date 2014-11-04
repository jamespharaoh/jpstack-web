package wbs.apn.chat.user.core.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class ChatUserDateLogRec
	implements CommonRecord<ChatUserDateLogRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ReferenceField
	ChatUserRec chatUser;

	// TODO

	// details

	@ReferenceField
	UserRec user;

	@ReferenceField
	MessageRec message;

	@SimpleField
	Date timestamp;

	@SimpleField
	ChatUserDateMode dateMode;

	@SimpleField
	Integer radius;

	@SimpleField
	Integer startHour;

	@SimpleField
	Integer endHour;

	@SimpleField
	Integer dailyMax;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserDateLogRec> otherRecord) {

		ChatUserDateLogRec other =
			(ChatUserDateLogRec) otherRecord;

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
