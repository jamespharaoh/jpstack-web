package wbs.ticket.model;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
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
	
	@CollectionField (
			orderBy = "index")
		Set<TicketNoteRec> ticketNotes =
			new TreeSet<TicketNoteRec> ();
	
	@SimpleField (
			nullable = true)
	TicketStateRec ticketState;
	
	@SimpleField (
			nullable = true)
	Instant nextActionDate;
	
	// statistics

	@SimpleField
	Integer numNotes = 0;
	
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
