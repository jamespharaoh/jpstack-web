package wbs.services.messagetemplate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MinorEntity
public
class MessageTemplateParameterRec
	implements MinorRecord<MessageTemplateParameterRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	MessageTemplateTypeRec messageTemplateType;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@SimpleField
	Boolean required;

	@SimpleField (
		nullable = true)
	Integer length;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateParameterRec> otherRecord) {

		MessageTemplateParameterRec other =
			(MessageTemplateParameterRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getName (),
				other.getName ())

			.append (
				getMessageTemplateType (),
				other.getMessageTemplateType ())

			.toComparison ();

	}

}
