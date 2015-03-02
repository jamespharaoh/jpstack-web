package wbs.clients.apn.chat.tv.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.media.model.MediaRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatTvPicUploadedRec
	implements CommonRecord<ChatTvPicUploadedRec> {

	@ForeignIdField (
		field = "media")
	Integer id;

	@MasterField
	MediaRec media;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvPicUploadedRec> otherRecord) {

		ChatTvPicUploadedRec other =
			(ChatTvPicUploadedRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMedia (),
				other.getMedia ())

			.toComparison ();

	}

}
