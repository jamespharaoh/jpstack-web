package wbs.clients.apn.chat.contact.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatContactNoteRec
	implements EphemeralRecord<ChatContactNoteRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec user;

	@ReferenceField
	ChatUserRec monitor;

	@ReferenceField
	ChatRec chat;

	@SimpleField
	String notes = "";

	@SimpleField
	Date timestamp;

	@ReferenceField
	UserRec consoleUser;

	@SimpleField
	Boolean pegged = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatContactNoteRec> otherRecord) {

		ChatContactNoteRec other =
			(ChatContactNoteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getUser (),
				other.getUser ())

			.append (
				getMonitor (),
				other.getMonitor ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
