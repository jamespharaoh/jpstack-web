package wbs.clients.apn.chat.ad.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatAdTemplateRec
	implements EphemeralRecord<ChatAdTemplateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	// settings

	@ReferenceField
	TextRec genericText;

	@ReferenceField
	TextRec gayMaleText;

	@ReferenceField
	TextRec gayFemaleText;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatAdTemplateRec> otherRecord) {

		ChatAdTemplateRec other =
			(ChatAdTemplateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
