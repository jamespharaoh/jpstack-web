package wbs.ticket.model;

import java.util.Random;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.ticket.model.TicketRec.TicketObjectHelperMethods;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class TicketFieldTypeRec  
	implements CommonRecord<TicketFieldTypeRec> {
	
	// id
	
	@GeneratedIdField
	Integer id;
	
	@CodeField
	String code;
	
	// details
	
	@SimpleField
	String type;
	
	@SimpleField (
			nullable = true)
	String objectType;
	
	@SimpleField
	Boolean required;
	
	// object helper methods
	
	public
	interface TicketFieldTypeObjectHelperMethods {
	
		String generateCode ();
	
	}
	
	// object helper implementation
	
	public static
	class TicketFieldTypeObjectHelperImplementation
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
			Record<TicketFieldTypeRec> otherRecord) {
	
		TicketFieldTypeRec other =
			(TicketFieldTypeRec) otherRecord;
	
		return new CompareToBuilder ()
	
			.append (
				getCode (),
				other.getCode ())
	
			.toComparison ();
	
	}

}