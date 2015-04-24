package wbs.services.ticket.core.model;

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
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;

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
	
	@ParentField
	TicketManagerRec ticketManager;
	
	// details
	
	@NameField
	String name;
	
	@SimpleField
	TicketStateState state;
	
	@SimpleField
	Boolean showInQueue;
	
	@SimpleField
	Integer minimum;
	
	@SimpleField
	Integer maximum;
	
	// object hooks

	public static
	class TicketStateHooks
		extends AbstractObjectHooks<TicketStateRec> {

		@Inject
		Provider<TicketFieldTypeObjectHelper> ticketFieldTypeHelper;

		@Inject
		Database database;
		
		@Inject
		RandomLogic randomLogic;

		@Override
		public
		void beforeInsert (
				TicketStateRec ticketState) {
			
			ticketState.setCode (
				ticketState.getName().toLowerCase());

		}

	}
	
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