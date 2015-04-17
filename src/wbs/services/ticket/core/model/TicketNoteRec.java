package wbs.services.ticket.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class TicketNoteRec
	implements CommonRecord<TicketNoteRec>{
	
	// id
	
	@GeneratedIdField
	Integer id;
	
	// identity
	
	@ParentField
	TicketRec ticket;
	
	@IndexField (
		counter = "numNotes")
	Integer index;
	
	// compare to
	
	@Override
	public
	int compareTo (
			Record<TicketNoteRec> otherRecord) {
	
		TicketNoteRec other =
			(TicketNoteRec) otherRecord;
	
		return new CompareToBuilder ()
	
			.append (
				getTicket (),
				other.getTicket ())
	
			.append (
				getIndex (),
				other.getIndex ())
	
			.toComparison ();
	
	}

}
