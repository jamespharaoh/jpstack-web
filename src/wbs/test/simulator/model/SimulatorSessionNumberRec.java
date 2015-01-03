package wbs.test.simulator.model;

import javax.inject.Inject;
import javax.inject.Provider;

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

	// object helper methods

	public static
	interface SimulatorSessionNumberObjectHelperMethods {

		SimulatorSessionNumberRec findOrCreate (
				NumberRec number);

	}

	// object helper implementation

	public static
	class SimulatorSessionNumberObjectHelperImplementation
		implements SimulatorSessionNumberObjectHelperMethods {

		// indirect dependencies

		@Inject
		Provider<SimulatorSessionNumberObjectHelper>
		simulatorSessionNumberHelperProvider;

		// implementation

		@Override
		public
		SimulatorSessionNumberRec findOrCreate (
				NumberRec number) {

			SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper =
				simulatorSessionNumberHelperProvider.get ();

			// find existing

			SimulatorSessionNumberRec existingSimulatorSessionNumber =
				simulatorSessionNumberHelper.find (
					number.getId ());

			if (existingSimulatorSessionNumber != null)
				return existingSimulatorSessionNumber;

			// create new

			SimulatorSessionNumberRec newSimulatorSessionNumber =
				simulatorSessionNumberHelper.insert (
					new SimulatorSessionNumberRec ()

				.setNumber (
					number)

			);

			return newSimulatorSessionNumber;

		}

	}

}
