package wbs.clients.apn.chat.tv.core.model;

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

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatTvCarouselRec
	implements CommonRecord<ChatTvCarouselRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField (column = "chat_id")
	ChatTvRec chatTv;

	@ReferenceField (column = "chat_tv_message_id")
	ChatTvMessageRec message;

	@SimpleField
	Date timestamp;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvCarouselRec> otherRecord) {

		ChatTvCarouselRec other =
			(ChatTvCarouselRec) otherRecord;

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
