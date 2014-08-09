package wbs.smsapps.alerts.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString (of = "id")
@EphemeralEntity
public
class AlertsSubjectRec
	implements EphemeralRecord<AlertsSubjectRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	AlertsSettingsRec alertsSettings;

	@ReferenceField
	ObjectTypeRec objectType;

	@SimpleField
	Integer objectId;

	@SimpleField
	Boolean include = true;

	// compare to

	@Override
	public
	int compareTo (
			Record<AlertsSubjectRec> otherRecord) {

		AlertsSubjectRec other =
			(AlertsSubjectRec) otherRecord;

		return new CompareToBuilder ()
			.append (getAlertsSettings (), other.getAlertsSettings ())
			.append (getId (), other.getId ())
			.toComparison ();

	}

}
