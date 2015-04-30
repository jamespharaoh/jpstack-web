package wbs.services.messagetemplate.model;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;
import wbs.services.messagetemplate.model.MessageTemplateTypeObjectHelper;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class MessageTemplateTypeRec  
	implements CommonRecord<MessageTemplateTypeRec> {
	
	// id
	
	@GeneratedIdField
	Integer id;
	
	@CodeField
	String code;
	
	@ParentField
	MessageTemplateDatabaseRec messageTemplateDatabase;
	
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
	
	// object hooks

	public static
	class MessageTemplateTypeHooks
		extends AbstractObjectHooks<MessageTemplateTypeRec> {

		@Inject
		Provider<MessageTemplateTypeObjectHelper> messageTemplateTypeHelper;

		@Inject
		Database database;
		
		@Inject
		RandomLogic randomLogic;

		@Override
		public
		void beforeInsert (
				MessageTemplateTypeRec messageTemplateType) {
			
			messageTemplateType.setCode (
				messageTemplateType.getName().toLowerCase());

		}

	}	
	
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
