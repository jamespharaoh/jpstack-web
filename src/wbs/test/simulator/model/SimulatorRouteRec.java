package wbs.test.simulator.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SimulatorRouteRec
	implements MinorRecord<SimulatorRouteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SimulatorRec simulator;

	@CodeField
	String prefix;

	// details

	@DescriptionField
	String description;

	@ReferenceField
	RouteRec route;

	// compare to

	@Override
	public
	int compareTo (
			Record<SimulatorRouteRec> otherRecord) {

		SimulatorRouteRec other =
			(SimulatorRouteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSimulator (),
				other.getSimulator ())

			.append (
				getPrefix (),
				other.getPrefix ())

			.toComparison ();

	}

}
