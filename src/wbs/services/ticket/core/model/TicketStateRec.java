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
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.RandomLogic;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MinorEntity
public class TicketStateRec
	implements MinorRecord<TicketStateRec> {

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