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
public 
class SmsArenaDlrReportLogRec 
	implements CommonRecord<SmsArenaDlrReportLogRec> {
	
	// id

	@GeneratedIdField
	Integer id;

	// details

	@ReferenceField
	RouteRec route;

	@SimpleField
	SmsArenaDlrReportLogType type;

	@SimpleField
	Instant timestamp;

	@SimpleField
	String details;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsArenaDlrReportLogRec> otherRecord) {

		SmsArenaDlrReportLogRec other =
			(SmsArenaDlrReportLogRec) otherRecord;

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
	interface SmsArenaDlrReportLogDaoMethods {

		List<Integer> searchIds (
				SmsArenaDlrReportLogSearch smsArenaDlrReportLogSearch);

	}

	// object hooks

	public static
	class SmsArenaDlrReportLogHooks
		extends AbstractObjectHooks<SmsArenaDlrReportLogRec> {

		// dependencies

		@Inject
		SmsArenaDlrReportLogDao smsArenaDlrReportLogDao;

		// implementation

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			SmsArenaDlrReportLogSearch smsArenaDlrReportLogSearch =
				(SmsArenaDlrReportLogSearch) search;

				return smsArenaDlrReportLogDao.searchIds (
					smsArenaDlrReportLogSearch);

		}

	}

	// search

	@Accessors (chain = true)
	@Data
	@EqualsAndHashCode
	@ToString
	public static
	class SmsArenaDlrReportLogSearch {

		Instant timestampAfter;
		Instant timestampBefore;

		Integer routeId;

	}

}
