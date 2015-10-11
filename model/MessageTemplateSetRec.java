package wbs.services.messagetemplate.model;

import java.util.Map;
import java.util.TreeMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

import com.google.common.collect.Ordering;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class MessageTemplateSetRec
	implements MajorRecord<MessageTemplateSetRec> {

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

	@DescriptionField
	String description;

	@CollectionField (
		index = "message_template_type_id")
	Map<Integer,MessageTemplateValueRec> messageTemplateValues =
		new TreeMap<Integer,MessageTemplateValueRec> (
			Ordering.arbitrary ());

	@SimpleField
	Integer numTemplates = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageTemplateSetRec> otherRecord) {

		MessageTemplateSetRec other =
			(MessageTemplateSetRec) otherRecord;

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
