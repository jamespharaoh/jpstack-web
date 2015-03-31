package wbs.integrations.oxygen8.model;

import java.util.List;

import javax.inject.Inject;

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
import wbs.framework.object.AbstractObjectHooks;
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

	// dao methods

	public static
	interface Oxygen8InboundLogDaoMethods {

		List<Integer> searchIds (
				Oxygen8InboundLogSearch oxygen8InboundLogSearch);

	}

	// object hooks

	public static
	class Oxygen8InboundLogHooks
		extends AbstractObjectHooks<Oxygen8InboundLogRec> {

		// dependencies

		@Inject
		Oxygen8InboundLogDao oxygen8InboundLogDao;

		// implementation

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			Oxygen8InboundLogSearch oxygen8InboundLogSearch =
				(Oxygen8InboundLogSearch) search;

			return oxygen8InboundLogDao.searchIds (
				oxygen8InboundLogSearch);

		}

	}

	// search

	@Accessors (chain = true)
	@Data
	@EqualsAndHashCode
	@ToString
	public static
	class Oxygen8InboundLogSearch {

		Instant timestampAfter;
		Instant timestampBefore;

		Integer routeId;

	}

}
