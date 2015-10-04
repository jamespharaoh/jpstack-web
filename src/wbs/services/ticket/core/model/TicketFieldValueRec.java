package wbs.services.ticket.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

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
public class TicketFieldValueRec
	implements CommonRecord<TicketFieldValueRec> {

	// id

	@GeneratedIdField
	Integer id;

	@ParentField
	TicketRec ticket;

	@IdentityReferenceField
	TicketFieldTypeRec ticketFieldType;

	// details

	@SimpleField (
		nullable = true)
	String stringValue;

	@SimpleField (
		nullable = true)
	Integer integerValue;

	@SimpleField (
			nullable = true)
	Boolean booleanValue;

	// compare to

	@Override
	public
	int compareTo (
			Record<TicketFieldValueRec> otherRecord) {

		TicketFieldValueRec other =
			(TicketFieldValueRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
