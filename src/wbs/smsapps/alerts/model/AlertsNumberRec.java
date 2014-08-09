package wbs.smsapps.alerts.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class AlertsNumberRec
	implements EphemeralRecord<AlertsNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	AlertsSettingsRec alertsSettings;

	// TODO?

	// details

	@SimpleField
	String name;

	@ReferenceField
	NumberRec number;

	@SimpleField
	Boolean enabled;

	// compare to

	@Override
	public
	int compareTo (
			Record<AlertsNumberRec> otherRecord) {

		AlertsNumberRec other =
			(AlertsNumberRec) otherRecord;

		return new CompareToBuilder ()
			.append (getAlertsSettings (), other.getAlertsSettings ())
			.append (getId (), other.getId ())
			.toComparison ();

	}

}