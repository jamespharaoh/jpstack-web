package wbs.test.simulator.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SimulatorSessionRec
	implements CommonRecord<SimulatorSessionRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SimulatorRec simulator;

	// details

	@DescriptionField
	String description;

	@SimpleField
	Instant createdTime;

	@ReferenceField
	UserRec createdUser;

	// compare to

	@Override
	public
	int compareTo (
			Record<SimulatorSessionRec> otherRecord) {

		SimulatorSessionRec other =
			(SimulatorSessionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
