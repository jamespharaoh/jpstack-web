package wbs.smsapps.manualresponder.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;


import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;

import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

import wbs.platform.user.model.UserRec;

import java.util.List;

import org.joda.time.Interval;


@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity (
	create = false)
public
class ManualResponderReportRec
	implements CommonRecord<ManualResponderReportRec> {

	// id

	@ForeignIdField (field = "manualResponderRequest")
	Integer id;

	// identity

	@MasterField
	ManualResponderRequestRec manualResponderRequest;

	// details

	@ReferenceField
	ManualResponderRec manualResponder;

	@ReferenceField
	UserRec user;

	@SimpleField
	Integer num;

	// compare to

	@Override
	public
	int compareTo (
			Record<ManualResponderReportRec> otherRecord) {

		ManualResponderReportRec other =
			(ManualResponderReportRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

	public static
	interface ManualResponderReportDaoMethods {

		List<ManualResponderReportRec> find (
				Interval timestampInterval);

	}


}
