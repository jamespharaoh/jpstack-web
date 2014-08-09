package wbs.smsapps.forwarder.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ForwarderRec
	implements MajorRecord<ForwarderRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	@DeletedField
	Boolean deleted = false;

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@SimpleField
	Integer maxReplies = 0;

	@SimpleField
	Integer maxFreeReplies = 0;

	@SimpleField
	Integer maxBillReplies = 0;

	@SimpleField
	Boolean allowNewSends = false;

	@SimpleField
	Boolean allowNullOtherId = false;

	@SimpleField
	String password = "";

	@SimpleField
	String url = "";

	@SimpleField
	String urlParams = "";

	@SimpleField
	Boolean urlPost = true;

	@SimpleField
	Integer maxPri = 0;

	@SimpleField
	Integer defaultPri = 0;

	@SimpleField
	Integer inboundTimeoutSecs = 0;

	@SimpleField
	Integer outboundTimeoutSecs = 0;

	@SimpleField
	Boolean reportEnabled = false;

	@SimpleField
	String reportUrl = "";

	@ReferenceField (nullable = true)
	SmsTrackerRec smsTracker;

	@CollectionField
	Set<ForwarderRouteRec> forwarderRoutes =
		new HashSet<ForwarderRouteRec> ();

	@Override
	public
	int compareTo (
			Record<ForwarderRec> otherRecord) {

		ForwarderRec other =
			(ForwarderRec) otherRecord;

		return new CompareToBuilder ()
			.append (getSlice (), other.getSlice ())
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
