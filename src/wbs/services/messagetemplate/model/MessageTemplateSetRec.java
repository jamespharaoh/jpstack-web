package wbs.services.messagetemplate.model;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.TransientObjectException;

import com.google.common.collect.Ordering;

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
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;
import wbs.platform.queue.logic.QueueLogic;

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
			index = "message_template_type_id")
		Map<Integer,MessageTemplateValueRec> messageTemplateValues =
			new TreeMap<Integer,MessageTemplateValueRec> (
				Ordering.arbitrary ());
	
	@SimpleField
	Integer numTemplates = 0;
	
	// object hooks

	public static
	class MessageTemplateSetHooks
		extends AbstractObjectHooks<MessageTemplateSetRec> {

		@Inject
		Database database;
		
		@Inject
		RandomLogic randomLogic;
		
		@Inject
		Provider<ObjectManager> objectManager;
		
		@Inject
		Provider<QueueLogic> queueLogic;
		
		@Inject
		Provider<MessageTemplateSetObjectHelper> messageTemplateSetHelper;
		
		@Inject
		Provider<MessageTemplateTypeObjectHelper> messageTemplateTypeHelper;	
		
		@Inject
		Provider<MessageTemplateValueObjectHelper> messageTemplateValueHelper;	

		@Override
		public
		void beforeInsert (
				MessageTemplateSetRec messageTemplateSet) {
			
			messageTemplateSet.setCode (
				randomLogic.generateNumericNoZero (8));
			
		}
			
		@Override
		public
		Object getDynamic (
				Record<?> object,
				String name) {
			
			MessageTemplateSetRec messageTemplateSet = 
				(MessageTemplateSetRec) object;
			
			//Find the ticket field type
			
			MessageTemplateTypeRec messageTemplateType =			
				messageTemplateTypeHelper.get().findByCode(
					messageTemplateSet.getMessageTemplateDatabase(), 
					name);

			try {
				
				//Find the message template value
				
				MessageTemplateValueRec messageTemplateValue =
					messageTemplateSet.getMessageTemplateValues().get( 
						messageTemplateType.getId());
							
				if (messageTemplateValue == null) { 
					return messageTemplateType.getDefaultValue(); 
				}
				else {				
					return messageTemplateValue.getStringValue();
				}
						

			} catch (TransientObjectException exception) {

				// object not yet saved so fields will all be null
				
				return null;

			}
				
		}

		@Override
		public
		void setDynamic (
				Record<?> object,
				String name,
				Object value) {
				
			MessageTemplateSetRec messageTemplateSet = 
				(MessageTemplateSetRec) object;
			
			//Find the ticket field type
			
			MessageTemplateTypeRec messageTemplateType =			
				messageTemplateTypeHelper.get().findByCode(
						messageTemplateSet.getMessageTemplateDatabase(), 
						name);	
			
			String message = (String) value;
			
			if (message.length () < messageTemplateType.getMinLength () ||
				message.length () > messageTemplateType.getMaxLength ()) {				
					throw new RuntimeException ("The message length is out of it's template type bounds!");
			}
			
			MessageTemplateValueRec messageTemplateValue;
			
			try {		
				
				messageTemplateValue =
					messageTemplateSet.getMessageTemplateValues().get( 
						messageTemplateType.getId());
			}
			catch (Exception e) {
				messageTemplateValue =
					null;
			}
			
			// if the value object does not exist, a new one is created
			
			if (messageTemplateValue == null) {
				messageTemplateValue = new MessageTemplateValueRec()					
					.setMessageTemplateSet(messageTemplateSet)
					.setMessageTemplateType(messageTemplateType);
			}
			
			
			messageTemplateValue.setStringValue((String)message);					
			
			messageTemplateSet.setNumTemplates (
				messageTemplateSet.getNumTemplates() + 1);
			
			 messageTemplateSet.getMessageTemplateValues ().put (
				messageTemplateType.getId(), 
				messageTemplateValue);
				
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
