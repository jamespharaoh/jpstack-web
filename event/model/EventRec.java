package wbs.platform.event.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class EventRec
	implements CommonRecord<EventRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	EventTypeRec eventType;

	@SimpleField (
		sqlType = "timestamp with time zone")
	Date timestamp;

	// TODO should be list
	@CollectionField (
		index = "index",
		orderBy = "index")
	Map<Integer,EventLinkRec> eventLinks =
		new HashMap<Integer,EventLinkRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<EventRec> otherRecord) {

		EventRec other =
			(EventRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
