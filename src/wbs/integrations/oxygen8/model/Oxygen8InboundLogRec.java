package wbs.integrations.oxygen8.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class Oxygen8InboundLogRec
	implements CommonRecord<Oxygen8InboundLogRec> {

	// id

	@GeneratedIdField
	Integer id;

	// details

	@ReferenceField
	RouteRec route;

	@SimpleField
	Oxygen8InboundLogType type;

	@SimpleField
	Instant timestamp;

	@SimpleField
	String details;

	// compare to

	@Override
	public
	int compareTo (
			Record<Oxygen8InboundLogRec> otherRecord) {

		Oxygen8InboundLogRec other =
			(Oxygen8InboundLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// search

	@Accessors (chain = true)
	@Data
	@EqualsAndHashCode
	@ToString
	public static
	class Oxygen8InboundLogSearch {

		Integer routeId;

		Instant timestampAfter;
		Instant timestampBefore;

		String details;

	}

}
