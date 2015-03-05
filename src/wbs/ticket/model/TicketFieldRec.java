package wbs.ticket.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class TicketFieldRec 
	implements CommonRecord<TicketFieldRec> {
	
	// id
	
	@GeneratedIdField
	Integer id;
	
	@IndexField (
		counter = "numFields")
	Integer index;
	
	// details
	
	@SimpleField
	String type;
	
	@SimpleField (
			nullable = true)
	String objectType;
	
	@SimpleField
	boolean required;
	
	// compare to
	
	@Override
	public
	int compareTo (
			Record<TicketFieldRec> otherRecord) {
	
		TicketFieldRec other =
			(TicketFieldRec) otherRecord;
	
		return new CompareToBuilder ()
	
			.append (
				getIndex (),
				other.getIndex ())
	
			.toComparison ();
	
	}

}