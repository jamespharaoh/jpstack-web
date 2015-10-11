package wbs.clients.apn.chat.namednote.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.joda.time.DateTime;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
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
class ChatNamedNoteRec
	implements CommonRecord<ChatNamedNoteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec thisUser;

	@IdentityReferenceField
	ChatUserRec otherUser;

	@IdentityReferenceField
	ChatNoteNameRec chatNoteName;

	// details

	@ReferenceField
	UserRec user;

	@SimpleField (
		hibernateTypeHelper = PersistentDateTime.class,
		sqlType = "timestamp with time zone")
	DateTime timestamp;

	@ReferenceField
	TextRec text;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatNamedNoteRec> otherRecord) {

		ChatNamedNoteRec other =
			(ChatNamedNoteRec) otherRecord;

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
