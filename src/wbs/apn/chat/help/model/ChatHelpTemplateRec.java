package wbs.apn.chat.help.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.TypeCodeField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatHelpTemplateRec
	implements EphemeralRecord<ChatHelpTemplateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@TypeCodeField
	String type;

	@CodeField
	String code;

	// details

	@DescriptionField
	String description = "";

	// settings

	@SimpleField
	String text = "";

	// TODO should this be here?
	@SimpleField (
		nullable = true)
	String fromNumber;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatHelpTemplateRec> otherRecord) {

		ChatHelpTemplateRec other =
			(ChatHelpTemplateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getType (),
				other.getType ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
