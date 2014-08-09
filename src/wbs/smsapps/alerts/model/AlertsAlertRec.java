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
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class AlertsAlertRec
	implements CommonRecord<AlertsAlertRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	AlertsSettingsRec alertsSettings;

	@IndexField
	Integer index;

	// info

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant timestamp;

	// data

	@SimpleField
	Integer unclaimedItems;

	@SimpleField
	Integer maximumDuration;

	@ReferenceField
	TextRec text;

	@SimpleField
	Integer recipients;

	// compare to

	@Override
	public
	int compareTo (
			Record<AlertsAlertRec> otherRecord) {

		AlertsAlertRec other =
			(AlertsAlertRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getAlertsSettings (),
				other.getAlertsSettings ())

			.append (
				other.getIndex (),
				getIndex ())

			.toComparison ();

	}

}
