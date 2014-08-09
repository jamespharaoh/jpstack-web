package wbs.smsapps.alerts.model;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp;
import org.joda.time.Instant;

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
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class AlertsSettingsRec
	implements MajorRecord<AlertsSettingsRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// info

	@DeletedField
	Boolean deleted = false;

	@NameField
	String name;

	@DescriptionField
	String description;

	// settings

	@SimpleField
	Boolean enabled = false;

	@ReferenceField (
		nullable = true)
	RouterRec router;

	@SimpleField
	String numFrom = "Alerts";

	@SimpleField
	String template =
		"Queue alert: {numItems} items, {numMinutes} minutes";

	@SimpleField
	Integer maxItemsInQueue = 100;

	@SimpleField
	Integer maxDurationInQueue = 600;

	@SimpleField
	Integer maxAlertFrequency = 3600;

	@SimpleField (
		nullable = true)
	Integer startHour;

	@SimpleField (
		nullable = true)
	Integer endHour;

	// data

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class,
		nullable = true)
	Instant lastStatusCheck;

	@SimpleField
	Integer numStatusChecks = 0;

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class,
		nullable = true)
	Instant lastAlert;

	@SimpleField
	Integer numAlerts = 0;

	// children

	@CollectionField
	Set<AlertsNumberRec> alertsNumbers =
		new LinkedHashSet<AlertsNumberRec> ();

	@CollectionField
	Set<AlertsSubjectRec> alertsSubjects =
		new LinkedHashSet<AlertsSubjectRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<AlertsSettingsRec> otherRecord) {

		AlertsSettingsRec other =
			(AlertsSettingsRec) otherRecord;

		return new CompareToBuilder ()
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
