package wbs.platform.postgresql.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class PostgresqlMaintenanceRec
	implements EphemeralRecord<PostgresqlMaintenanceRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	// TODO properly

	@SimpleField
	PostgresqlMaintenanceFrequency frequency;

	@SimpleField
	Integer sequence;

	// settings

	@SimpleField
	String command;

	@SimpleField (
		nullable = true)
	Date lastRun;

	@SimpleField (
		nullable = true)
	Integer lastDuration;

	@SimpleField (
		nullable = true)
	String lastOutput;

	// compare to

	@Override
	public
	int compareTo (
			Record<PostgresqlMaintenanceRec> otherRecord) {

		PostgresqlMaintenanceRec other =
			(PostgresqlMaintenanceRec) otherRecord;

		return new CompareToBuilder ()
			.append (getFrequency (), other.getFrequency ())
			.append (getSequence (), other.getSequence ())
			.append (getCommand (), other.getCommand ())
			.append (getId (), other.getId ())
			.toComparison ();

	}

}
