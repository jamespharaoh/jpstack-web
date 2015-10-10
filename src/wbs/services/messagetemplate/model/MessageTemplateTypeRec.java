package wbs.services.messagetemplate.model;

import java.util.Set;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
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
class MessageTemplateTypeRec
	implements MinorRecord<MessageTemplateTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	MessageTemplateDatabaseRec messageTemplateDatabase;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@SimpleField
	String defaultValue;

	@SimpleField
	String helpText;

	@SimpleField
	Integer minLength;

	@SimpleField
	Integer maxLength;

	@SimpleField
	MessageTemplateTypeCharset charset;

	@SimpleField
	Integer numParameters = 0;

	// children

	@CollectionField (
		orderBy = "name")
	Set<MessageTemplateParameterRec> messageTemplateParameters =
		new TreeSet<MessageTemplateParameterRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateTypeRec> otherRecord) {

		MessageTemplateTypeRec other =
			(MessageTemplateTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.append (
				getMessageTemplateDatabase (),
				other.getMessageTemplateDatabase ())

			.toComparison ();

	}

}
