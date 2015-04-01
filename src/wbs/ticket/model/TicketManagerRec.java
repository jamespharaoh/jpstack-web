package wbs.ticket.model;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class TicketManagerRec
	implements CommonRecord<TicketManagerRec> {
	
	// id
	
	@GeneratedIdField
	Integer id;
	
	// identity
	
	@ParentField
	SliceRec slice;
	
	@CodeField
	String code;
	
	// details
	
	@CollectionField (
			orderBy = "id")
		Set<TicketFieldTypeRec> ticketFieldTypes =
			new TreeSet<TicketFieldTypeRec> ();
	
	// object helper methods
	
	public
	interface TicketManagerObjectHelperMethods {
	
		String generateCode ();
	
	}
	
	// object helper implementation
	
	public static
	class TicketManagerObjectHelperImplementation
		implements TicketManagerObjectHelperMethods {
	
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
			Record<TicketManagerRec> otherRecord) {
	
		TicketManagerRec other =
			(TicketManagerRec) otherRecord;
	
		return new CompareToBuilder ()
	
			.append (
				getCode (),
				other.getCode ())
	
			.append (
				getSlice (),
				other.getSlice ())
	
			.toComparison ();

	}

}
