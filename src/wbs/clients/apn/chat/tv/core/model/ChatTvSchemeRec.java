package wbs.clients.apn.chat.tv.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
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
class ChatTvSchemeRec
	implements MajorRecord<ChatTvSchemeRec> {

	// id

	@ForeignIdField (
		field = "chatScheme")
	Integer id;

	// identity

	@MasterField
	ChatSchemeRec chatScheme;

	// statistics

	@SimpleField
	Integer toScreenPhotoCharge = 0;

	@SimpleField
	Integer toScreenTextCharge = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvSchemeRec> otherRecord) {

		ChatTvSchemeRec other =
			(ChatTvSchemeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatScheme (),
				other.getChatScheme ())

			.toComparison ();

	}

}
