package wbs.clients.apn.chat.contact.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.media.model.MediaRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatMessageRec
	implements CommonRecord<ChatMessageRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatRec chat;

	@ReferenceField (
		nullable = true)
	ChatContactRec chatContact;

	@SimpleField (
		nullable = true)
	Integer index;

	@ReferenceField
	ChatUserRec fromUser;

	@ReferenceField
	ChatUserRec toUser;

	@SimpleField
	Date timestamp = new Date ();

	@SimpleField (
		nullable = true)
	Date moderatorTimestamp;

	@SimpleField (
		nullable = true)
	Integer threadId;

	@SimpleField (
		nullable = true)
	ChatMessageMethod source;

	@SimpleField (nullable = true)
	ChatMessageMethod method = ChatMessageMethod.sms;

	@ReferenceField
	TextRec originalText;

	@ReferenceField (
		nullable = true)
	TextRec editedText;

	@SimpleField
	Boolean monitorWarning = false;

	@ReferenceField (
		column = "sender_user_id",
		nullable = true)
	UserRec sender;

	@ReferenceField (
		column = "moderator_user_id",
		nullable = true)
	UserRec moderator;

	@SimpleField
	ChatMessageStatus status;

	@ReferenceField (
		nullable = true)
	QueueItemRec queueItem;

	@SimpleField (
		nullable = true)
	Integer deliveryId;

	@LinkField (
		table = "chat_message_media",
		index = "index")
	List<MediaRec> medias =
		new ArrayList<MediaRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatMessageRec> otherRecord) {

		ChatMessageRec other =
			(ChatMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
