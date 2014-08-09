package wbs.apn.chat.tv.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatTvModerationRec
	implements CommonRecord<ChatTvModerationRec> {

	@ForeignIdField (
		field = "chatUser")
	Integer id;

	@MasterField
	ChatUserRec chatUser;

	@ReferenceField
	QueueItemRec queueItem;

	@SimpleField
	Integer messageCount;

	@ReferenceField (
		column = "chat_tv_message_id")
	ChatTvMessageRec message;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvModerationRec> otherRecord) {

		ChatTvModerationRec other =
			(ChatTvModerationRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatUser (),
				other.getChatUser ())

			.toComparison ();

	}

}
