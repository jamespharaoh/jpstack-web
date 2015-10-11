package wbs.services.ticket.core.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;

import com.google.common.collect.Ordering;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@CommonEntity
public class TicketRec
	implements CommonRecord<TicketRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	TicketManagerRec ticketManager;

	@CodeField
	String code;

	// details

	@SimpleField
	Boolean queued =
		true;

	@ReferenceField
	TicketStateRec ticketState;

	@SimpleField
	Instant timestamp =
		Instant.now();

	@CollectionField (
			orderBy = "index")
		Set<TicketNoteRec> ticketNotes =
			new TreeSet<TicketNoteRec> ();

	@CollectionField (
			index = "ticket_field_type_id")
		Map<Integer,TicketFieldValueRec> ticketFieldValues =
			new TreeMap<Integer,TicketFieldValueRec> (
				Ordering.arbitrary ());

	// statistics

	@SimpleField
	Integer numNotes = 0;

	@SimpleField
	Integer numFields = 0;

	// state

	@ReferenceField (
			nullable = true)
		QueueItemRec queueItem;

	// children

	@CollectionField
	Set<TicketTemplateRec> templates =
		new LinkedHashSet<TicketTemplateRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<TicketRec> otherRecord) {

		TicketRec other =
			(TicketRec) otherRecord;

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
