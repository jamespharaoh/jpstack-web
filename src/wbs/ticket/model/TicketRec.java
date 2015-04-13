package wbs.ticket.model;

import java.util.Random;
import java.util.Set;
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

import com.google.common.collect.Ordering;

import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

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
	
	@ReferenceField (
			nullable = true)
	TicketStateRec ticketState;
	
	@SimpleField (
			nullable = true)
	Instant nextActionDate;
	
	@CollectionField (
			orderBy = "index")
		Set<TicketNoteRec> ticketNotes =
			new TreeSet<TicketNoteRec> ();
	
	@CollectionField
		Set<TicketFieldValueRec> ticketFieldValues =
			new TreeSet<TicketFieldValueRec> (
				Ordering.arbitrary ());
	
	// statistics

	@SimpleField
	Integer numNotes = 0;
	
	@SimpleField
	Integer numFields = 0;
	
	// object helper methods
	
	public
	interface TicketObjectHelperMethods {
	
		String generateCode ();
	
	}
	
	// object helper implementation
	
	public static
	class TicketObjectHelperImplementation
		implements TicketObjectHelperMethods {
	
		// dependencies
	
		@Inject
		Random random;
	
		// implementation
	
		@Override
		public
		String generateCode () {
	
			int intCode =
				+ random.nextInt (90000000)
				+ 10000000;
	
			return Integer.toString (
				intCode);
	
		}
	
	}
	
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
		Database database;

		@Override
		public
		void beforeInsert (
				TicketRec ticket) {

			ticket.setCode(ticketHelper.get()
					.generateCode());

		}
		
		@Override
		public
		Object getDynamic (
				Record<?> object,
				String name) {
			
			TicketRec ticket = (TicketRec) object;
			
			//Find the ticket field type
			
			TicketFieldTypeRec ticketFieldType =			
				ticketFieldTypeHelper.get().findByCode(
						ticket.getTicketManager(), 
						name);

			try {
				
				//Find the ticket fueld value
				
				TicketFieldValueRec ticketFieldValue =
						ticketHelper.get().findTicketFieldValue(
							ticket,
							ticketFieldType);	
				
				if (ticketFieldValue == null) { return null; }
				
				switch( ticketFieldType.getType() ) {
				
					case string:
						return ticketFieldValue.getStringValue();
						
					case number:
						return ticketFieldValue.getIntegerValue();
						
					case bool:
						return ticketFieldValue.getBooleanValue();
						
					case object:
						throw new RuntimeException ("TODO");
						
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
				ticketFieldValue = 
					ticketHelper.get().findTicketFieldValue(
						ticket,
						ticketFieldType);	
			}
			catch (Exception e) {
				ticketFieldValue =
					null;
			}
			
			// if the value object does not exist, a new one is created
			
			if (ticketFieldValue == null) {
				ticketFieldValue = new TicketFieldValueRec();
			}
			
			switch( ticketFieldType.getType() ) {
				case string:					
					ticketFieldValue
						.setStringValue((String)value)
						.setTicket(ticket)
						.setTicketFieldType(ticketFieldType);
					break;
					
				case number:
					ticketFieldValue
						.setIntegerValue((Integer)value)
						.setTicket(ticket)
						.setTicketFieldType(ticketFieldType);
					break;
					
				case bool:
					ticketFieldValue
						.setBooleanValue((Boolean)value)
						.setTicket(ticket)
						.setTicketFieldType(ticketFieldType);
					break;
					
				case object:
					throw new RuntimeException ("TODO");
					
				default:
					throw new RuntimeException ();
			
			}		
					
			ticket.getTicketFieldValues ().add (
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
