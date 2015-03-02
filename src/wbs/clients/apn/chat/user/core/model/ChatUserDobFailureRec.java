package wbs.clients.apn.chat.user.core.model;

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
import wbs.platform.text.model.TextRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserDobFailureRec
	implements CommonRecord<ChatUserDobFailureRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	MessageRec message;

	@SimpleField
	Date timestamp;

	@ReferenceField
	TextRec failingText;

	@Override
	public
	int compareTo (
			Record<ChatUserDobFailureRec> otherRecord) {

		ChatUserDobFailureRec other =
			(ChatUserDobFailureRec) otherRecord;

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
