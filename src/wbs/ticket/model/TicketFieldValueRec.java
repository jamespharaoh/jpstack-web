package wbs.ticket.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ReferenceField;
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
		
		@IndexField
		Integer index;
		
		// details
		
		@SimpleField
		String value;
		
		@ReferenceField
		TicketFieldTypeRec ticketTypeField;
		
		// compare to
		
		@Override
		public
		int compareTo (
				Record<TicketFieldValueRec> otherRecord) {
		
			TicketFieldValueRec other =
				(TicketFieldValueRec) otherRecord;
		
			return new CompareToBuilder ()
		
				.append (
					getIndex (),
					other.getIndex ())
		
				.toComparison ();
		
		}

}
