package wbs.smsapps.alerts.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp;
import org.joda.time.Instant;

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
class AlertsStatusCheckRec
	implements CommonRecord<AlertsStatusCheckRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	AlertsSettingsRec alertsSettings;

	@IndexField
	Integer index;

	// references

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant timestamp;

	// data

	@SimpleField
	Integer unclaimedItems;

	@SimpleField
	Integer maximumDuration;

	@SimpleField
	Boolean result;

	@SimpleField
	Boolean active;

	@SimpleField
	Boolean sentRecently;

	@SimpleField
	Boolean alertSent;

	// compare to

	@Override
	public
	int compareTo (
			Record<AlertsStatusCheckRec> otherRecord) {

		AlertsStatusCheckRec other =
			(AlertsStatusCheckRec) otherRecord;

		return new CompareToBuilder ()
			.append (getAlertsSettings (), other.getAlertsSettings ())
			.append (other.getIndex (), getIndex ())
			.toComparison ();

	}

}
