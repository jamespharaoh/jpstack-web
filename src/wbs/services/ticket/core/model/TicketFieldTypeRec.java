package wbs.services.ticket.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MinorEntity
public class TicketFieldTypeRec
	implements MinorRecord<TicketFieldTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	TicketManagerRec ticketManager;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@SimpleField
	Boolean required;

	@SimpleField
	TicketFieldTypeType type;

	@ReferenceField (
			nullable = true)
	ObjectTypeRec objectType;

	@SimpleField
	Boolean visible;

	// compare to

	@Override
	public
	int compareTo (
			Record<TicketFieldTypeRec> otherRecord) {

		TicketFieldTypeRec other =
			(TicketFieldTypeRec) otherRecord;

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