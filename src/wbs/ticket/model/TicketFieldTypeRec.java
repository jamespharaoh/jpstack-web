package wbs.ticket.model;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

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
	
	@ParentField
	TicketManagerRec ticketManager;
	
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
	
	// object helper methods
	
	public
	interface TicketFieldTypeObjectHelperMethods {
	
		String generateCode ();
	
	}
	
	// object helper implementation
	
	public static
	class TicketFieldTypeObjectHelperImplementation
		implements TicketFieldTypeObjectHelperMethods {
	
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
	
	// object hooks

	public static
	class TicketFieldTypeHooks
		extends AbstractObjectHooks<TicketFieldTypeRec> {

		@Inject
		Provider<TicketFieldTypeObjectHelper>  ticketFieldTypeHelper;

		@Inject
		Database database;

		@Override
		public
		void beforeInsert (
				TicketFieldTypeRec ticketFieldType) {
			
			ticketFieldType.setCode(ticketFieldTypeHelper.get()
					.generateCode());

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