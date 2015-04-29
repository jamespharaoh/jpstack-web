package wbs.services.messagetemplates.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class MessageTemplateValueRec  
	implements CommonRecord<MessageTemplateValueRec> {
	
	// id

	@GeneratedIdField
	Integer id;
	
	@ParentField
	MessageTemplateDatabaseRec messageTemplateDatabase;
	
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
				getMessageTemplateDatabase (),
				other.getMessageTemplateDatabase ())
	
			.toComparison ();
	
	}

}
