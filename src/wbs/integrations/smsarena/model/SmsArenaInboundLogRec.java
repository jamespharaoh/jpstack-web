package wbs.integrations.smsarena.model;

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
public class SmsArenaInboundLogRec 
	implements CommonRecord<SmsArenaInboundLogRec> {
	
	// id

	@GeneratedIdField
	Integer id;

	// details

	@ReferenceField
	RouteRec route;

	@SimpleField
	SmsArenaInboundLogType type;

	@SimpleField
	Instant timestamp;

	@SimpleField
	String details;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsArenaInboundLogRec> otherRecord) {

		SmsArenaInboundLogRec other =
			(SmsArenaInboundLogRec) otherRecord;

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
	interface SmsArenaInboundLogDaoMethods {

		List<Integer> searchIds (
				SmsArenaInboundLogSearch smsArenaInboundLogSearch);

	}

	// object hooks

	public static
	class SmsArenaInboundLogHooks
		extends AbstractObjectHooks<SmsArenaInboundLogRec> {

		// dependencies

		@Inject
		SmsArenaInboundLogDao smsArenaInboundLogDao;

		// implementation

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			SmsArenaInboundLogSearch smsArenaInboundLogSearch =
					(SmsArenaInboundLogSearch) search;

				return smsArenaInboundLogDao.searchIds (
					smsArenaInboundLogSearch);

		}

	}

	// search

	@Accessors (chain = true)
	@Data
	@EqualsAndHashCode
	@ToString
	public static
	class SmsArenaInboundLogSearch {

		Instant timestampAfter;
		Instant timestampBefore;

		Integer routeId;

	}

}
