package wbs.platform.event.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class EventLinkRec
	implements CommonRecord<EventLinkRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	EventRec event;

	@IndexField
	Integer index;

	@SimpleField
	Integer typeId;

	@SimpleField
	Integer refId;

	// compare to

	@Override
	public
	int compareTo (
			Record<EventLinkRec> otherRecord) {

		EventLinkRec other =
			(EventLinkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getEvent (),
				other.getEvent ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

}
