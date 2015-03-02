package wbs.clients.apn.chat.help.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class ChatHintLogRec
	implements CommonRecord<ChatHintLogRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatUserRec user;

	@ReferenceField
	ChatHintRec hint;

	@SimpleField
	Date timestamp;

	@Override
	public
	int compareTo (
			Record<ChatHintLogRec> otherRecord) {

		ChatHintLogRec other =
			(ChatHintLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.toComparison ();

	}

}
