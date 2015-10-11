package wbs.clients.apn.chat.contact.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatMonitorInboxRec
	implements EphemeralRecord<ChatMonitorInboxRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec monitorChatUser;

	@ReferenceField
	ChatUserRec userChatUser;

	@SimpleField
	Date timestamp = new Date ();

	@ReferenceField
	QueueItemRec queueItem;

	@SimpleField
	Boolean inbound = false;

	@SimpleField
	Boolean outbound = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatMonitorInboxRec> otherRecord) {

		ChatMonitorInboxRec other =
			(ChatMonitorInboxRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMonitorChatUser (),
				other.getMonitorChatUser ())

			.append (
				getUserChatUser (),
				other.getUserChatUser ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
