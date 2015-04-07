package wbs.ticket.model;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
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
	
	@CollectionField (
			orderBy = "index")
		Set<TicketFieldValueRec> ticketFieldValues =
			new TreeSet<TicketFieldValueRec> ();
	
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
		boolean getDynamic (
				Record<?> object,
				String name) {
			
			//TODO
			
			return false;
			
		}

		@Override
		public
		void setDynamic (
				Record<?> object,
				String name,
				Object value) {
			
			//TODO
			
		}
		
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
