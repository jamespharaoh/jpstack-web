package wbs.ticket.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class TicketStateRec 
	implements CommonRecord<TicketStateRec> {
		
	// id
	
	@GeneratedIdField
	Integer id;
	
	@CodeField
	String code;
	
	// details
	
	@SimpleField
	String state;
	
	// compare to
	
	@Override
	public
	int compareTo (
			Record<TicketStateRec> otherRecord) {
	
		TicketStateRec other =
			(TicketStateRec) otherRecord;
	
		return new CompareToBuilder ()
	
			.append (
				getCode (),
				other.getCode ())
	
			.toComparison ();
	
	}

}