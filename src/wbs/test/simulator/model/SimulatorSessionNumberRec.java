package wbs.test.simulator.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SimulatorSessionNumberRec
	implements EphemeralRecord<SimulatorSessionNumberRec> {

	// id

	@ForeignIdField (field = "number")
	Integer id;

	// identity

	@MasterField
	NumberRec number;

	// state

	@ReferenceField (
		nullable = true)
	SimulatorSessionRec simulatorSession;

	// compare to

	@Override
	public
	int compareTo (
			Record<SimulatorSessionNumberRec> otherRecord) {

		SimulatorSessionNumberRec other =
			(SimulatorSessionNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
