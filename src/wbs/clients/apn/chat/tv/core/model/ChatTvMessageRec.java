package wbs.clients.apn.chat.tv.core.model;

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
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatTvMessageRec
	implements CommonRecord<ChatTvMessageRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField (
		column = "chat_id")
	ChatTvRec chatTv;

	// details

	@ReferenceField
	ChatUserRec chatUser;

	@SimpleField
	Boolean textJockey;

	@ReferenceField
	TextRec originalText;

	@ReferenceField
	TextRec editedText;

	@SimpleField
	ChatTvMessageStatus status;

	@ReferenceField
	UserRec user;

	@SimpleField
	Date createdTime;

	@SimpleField
	Date moderatedTime;

	@SimpleField
	Date sentTime;

	@SimpleField
	Integer threadId;

	@ReferenceField
	MediaRec media;

	@SimpleField
	Date carouselTime;

	@SimpleField
	Integer carouselCount = 0;

	@Override
	public
	int compareTo (
			Record<ChatTvMessageRec> otherRecord) {

		ChatTvMessageRec other =
			(ChatTvMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}