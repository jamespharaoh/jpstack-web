package wbs.services.ticket.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@CommonEntity
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

	// data

	@SimpleField
	String noteText;

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
