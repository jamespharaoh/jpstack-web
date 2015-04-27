package wbs.services.ticket.core.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.TransientObjectException;
import org.joda.time.Instant;

import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldValueObjectHelper;
import wbs.services.ticket.core.model.TicketObjectHelper;

import com.google.common.collect.Ordering;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class TicketRec
	implements CommonRecord<TicketRec> {
	
	// id
	
	@GeneratedIdField
	Integer id;
	
	// identity
	
	@ParentField
	TicketManagerRec ticketManager;
	
	@CodeField
	String code;

	// details
	
	@ReferenceField
	TicketStateRec ticketState;
	
	@SimpleField (
			nullable = true)
	Instant nextActionDate;
	
	@CollectionField (
			orderBy = "index")
		Set<TicketNoteRec> ticketNotes =
			new TreeSet<TicketNoteRec> ();
	
	@CollectionField (
			index = "ticket_field_type_id")
		Map<Integer,TicketFieldValueRec> ticketFieldValues =
			new TreeMap<Integer,TicketFieldValueRec> (
				Ordering.arbitrary ());
	
	// statistics

	@SimpleField
	Integer numNotes = 0;
	
	@SimpleField
	Integer numFields = 0;
	
	// state
	
	@ReferenceField (
			nullable = true)
		QueueItemRec queueItem;
	
	// children

	@CollectionField
	Set<TicketTemplateRec> templates =
		new LinkedHashSet<TicketTemplateRec> ();
	
	// object hooks

	public static
	class TicketHooks
		extends AbstractObjectHooks<TicketRec> {

		@Inject
		Provider<TicketObjectHelper> ticketHelper;
		
		@Inject
		Provider<TicketFieldTypeObjectHelper> ticketFieldTypeHelper;	
		
		@Inject
		Provider<TicketFieldValueObjectHelper> ticketFieldValueHelper;	
		
		@Inject
		Provider<ObjectManager> objectManager;
		
		@Inject
		Provider<QueueLogic> queueLogic;
				
		@Inject
		Database database;
		
		@Inject
		RandomLogic randomLogic;
		
		@Override
		public
		void beforeInsert (
				TicketRec ticket) {

			ticket.setCode (
				randomLogic.generateNumericNoZero (8));

		}

		@Override
		public void afterInsert(TicketRec ticket) {
			
			if (ticket.getTicketState().getShowInQueue()) {				
	
				// create queue item
	
				QueueItemRec queueItem =
					queueLogic.get().createQueueItem (
						queueLogic.get().findQueue (
							ticket.getTicketState (),
							"default"),
						ticket,
						ticket,
						ticket.getCode (),
						ticket.getTicketState().toString());
	
				// add queue item to ticket
	
				ticket
					.setQueueItem (
						queueItem);
			
			}		

		}
		
		@Override
		public
		Object getDynamic (
				Record<?> object,
				String name) {
			
			TicketRec ticket = 
				(TicketRec) object;
			
			//Find the ticket field type
			
			TicketFieldTypeRec ticketFieldType =			
				ticketFieldTypeHelper.get().findByCode(
					ticket.getTicketManager(), 
					name);

			try {
				
				//Find the ticket fueld value
				
				TicketFieldValueRec ticketFieldValue =
					ticket.getTicketFieldValues().get( 
						ticketFieldType.getId());
				
				/*TicketFieldValueRec ticketFieldValue =
						ticketHelper.get().findTicketFieldValue(
							ticket,
							ticketFieldType);*/	
				
				if (ticketFieldValue == null) { return null; }
				
				switch( ticketFieldType.getType() ) {
				
					case string:
						return ticketFieldValue.getStringValue();
						
					case number:
						return ticketFieldValue.getIntegerValue();
						
					case bool:
						return ticketFieldValue.getBooleanValue();
						
					case object:						
						ObjectTypeRec objectType =
							ticketFieldType.getObjectType();
						
						Integer objectId =
							ticketFieldValue.getIntegerValue();
						
						Object obj = objectManager.get()
							.objectHelperForTypeId(objectType.getId())
								.find(objectId);
						
						return obj;
						
					default:
						throw new RuntimeException ();
			
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
				
			TicketRec ticket = 
				(TicketRec) object;
			
			//Find the ticket field type
			
			TicketFieldTypeRec ticketFieldType =			
				ticketFieldTypeHelper.get().findByCode(
						ticket.getTicketManager(), 
						name);			
			
			TicketFieldValueRec ticketFieldValue;
			
			try {
				/*ticketFieldValue = 
					ticketHelper.get().findTicketFieldValue(
						ticket,
						ticketFieldType);	*/				
				
				 ticketFieldValue =
					ticket.getTicketFieldValues().get( 
						ticketFieldType.getId());
			}
			catch (Exception e) {
				ticketFieldValue =
					null;
			}
			
			// if the value object does not exist, a new one is created
			
			if (ticketFieldValue == null) {
				ticketFieldValue = new TicketFieldValueRec()					
					.setTicket(ticket)
					.setTicketFieldType(ticketFieldType);
			}
			
			switch( ticketFieldType.getType() ) {
				case string:					
					ticketFieldValue.setStringValue((String)value);
					break;
					
				case number:
					ticketFieldValue.setIntegerValue((Integer)value);
					break;
					
				case bool:
					ticketFieldValue.setBooleanValue((Boolean)value);
					break;
					
				case object:					
					Integer objectId = 
						((Record<?>) value).getId();
					
					ticketFieldValue.setIntegerValue(objectId);
					break;
					
				default:
					throw new RuntimeException ();
			
			}		
			
			ticket.setNumFields (
				ticket.getNumFields() + 1);
			
			ticket.getTicketFieldValues ().put (
				ticketFieldType.getId(), 
				ticketFieldValue);
			
		}
		
	}
	
	// dao methods

	public static
	interface TicketDaoMethods {

		TicketFieldValueRec findTicketFieldValue (
				TicketRec ticket,
				TicketFieldTypeRec ticketFieldType);

	}
	
	// compare to
	
	@Override
	public
	int compareTo (
			Record<TicketRec> otherRecord) {
	
		TicketRec other =
			(TicketRec) otherRecord;
	
		return new CompareToBuilder ()
	
			.append (
				getTicketManager (),
				other.getTicketManager ())
	
			.append (
				getCode (),
				other.getCode ())
	
			.toComparison ();
	
	}

}
