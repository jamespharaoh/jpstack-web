package wbs.apn.chat.namednote.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.joda.time.DateTime;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
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
class ChatNamedNoteLogRec
	implements CommonRecord<ChatNamedNoteLogRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatNamedNoteRec chatNamedNote;

	@ReferenceField
	UserRec user;

	@SimpleField (
		hibernateTypeHelper = PersistentDateTime.class,
		sqlType = "timestamp with time zone")
	DateTime timestamp;

	@ReferenceField
	TextRec text;

	@Override
	public
	int compareTo (
			Record<ChatNamedNoteLogRec> otherRecord) {

		ChatNamedNoteLogRec other =
			(ChatNamedNoteLogRec) otherRecord;

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
