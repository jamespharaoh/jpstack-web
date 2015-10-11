package wbs.test.simulator.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class SimulatorEventRec
	implements CommonRecord<SimulatorEventRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SimulatorSessionRec simulatorSession;

	// details

	@SimpleField
	Instant timestamp;

	@SimpleField
	String type;

	@SimpleField
	String data;

	// compare to

	@Override
	public
	int compareTo (
			Record<SimulatorEventRec> otherRecord) {

		SimulatorEventRec other =
			(SimulatorEventRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
