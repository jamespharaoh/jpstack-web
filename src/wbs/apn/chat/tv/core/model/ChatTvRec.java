package wbs.apn.chat.tv.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ChatTvRec
	implements MajorRecord<ChatTvRec> {

	@ForeignIdField (
		field = "chat")
	Integer id;

	@MasterField
	ChatRec chat;

	@SimpleField
	String apiUrl;

	@SimpleField
	String imageUrl;

	@SimpleField
	String channelId;

	@SimpleField
	Integer toScreenTimeout;

	@SimpleField
	Boolean toScreenMedia;

	@SimpleField
	Integer toScreenTextDailyMax;

	@SimpleField
	Integer toScreenPhotoDailyMax;

	@SimpleField
	Integer carouselDelay;

	@SimpleField
	Integer carouselOldest;

	@SimpleField
	Integer carouselMinimum;

	@SimpleField
	Integer carouselMaximum;

	@SimpleField
	Integer carouselPercent;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvRec> otherRecord) {

		ChatTvRec other =
			(ChatTvRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.toComparison ();

	}

}
