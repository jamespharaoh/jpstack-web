package wbs.services.messagetemplate.model;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class MessageTemplateSetRec 
	implements CommonRecord<MessageTemplateSetRec> {
	
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
			orderBy = "id")
		Set<MessageTemplateValueRec> messageTemplateValues =
			new TreeSet<MessageTemplateValueRec> ();
	
	// object hooks

	public static
	class MessageTemplateSetHooks
		extends AbstractObjectHooks<MessageTemplateSetRec> {

		@Inject
		Provider<MessageTemplateSetObjectHelper> messageTemplateSetHelper;

		@Inject
		Database database;
		
		@Inject
		RandomLogic randomLogic;

		@Override
		public
		void beforeInsert (
				MessageTemplateSetRec messageTemplateSet) {
			
			messageTemplateSet.setCode (
				messageTemplateSet.getName().toLowerCase());

		}

	}	
	
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
