package wbs.services.messagetemplate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.MinorEntity;
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
class MessageTemplateValueRec
	implements MinorRecord<MessageTemplateValueRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	MessageTemplateSetRec messageTemplateSet;

	@IdentityReferenceField
	MessageTemplateTypeRec messageTemplateType;

	// details

	@SimpleField
	String stringValue;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateValueRec> otherRecord) {

		MessageTemplateValueRec other =
			(MessageTemplateValueRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.append (
				getMessageTemplateSet (),
				other.getMessageTemplateSet ())

			.toComparison ();

	}

}
