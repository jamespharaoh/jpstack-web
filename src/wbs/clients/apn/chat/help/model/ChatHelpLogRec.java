package wbs.clients.apn.chat.help.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatHelpLogRec
	implements CommonRecord<ChatHelpLogRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatUserRec chatUser;

	@ReferenceField (
		nullable = true)
	ChatHelpLogRec replyTo;

	@ReferenceField
	MessageRec message;

	@ReferenceField (
		nullable = true)
	CommandRec command;

	@ReferenceField (
		nullable = true)
	ChatMessageRec chatMessage;

	@SimpleField
	String text;

	@SimpleField
	MessageDirection direction;

	@SimpleField
	String ourNumber;

	@SimpleField
	Date timestamp =
		new Date ();

	@SimpleField (
		nullable = true)
	Date ignoredTime;

	@ReferenceField (
		nullable = true)
	UserRec user;

	@ReferenceField (
		nullable = true)
	UserRec ignoredUser;

	@ReferenceField (
		nullable = true)
	QueueItemRec queueItem;

	// compare to

	@Override
	public int compareTo (
			Record<ChatHelpLogRec> otherRecord) {

		ChatHelpLogRec other =
			(ChatHelpLogRec) otherRecord;

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
