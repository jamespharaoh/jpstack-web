package wbs.test.simulator.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.sms.number.core.model.NumberRec;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelper;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelperMethods;
import wbs.test.simulator.model.SimulatorSessionNumberRec;

public
class SimulatorSessionNumberObjectHelperMethodsImplementation
	implements SimulatorSessionNumberObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper;

	// implementation

	@Override
	public
	SimulatorSessionNumberRec findOrCreate (
			@NonNull NumberRec number) {

		// find existing

		Optional <SimulatorSessionNumberRec> existingSimulatorSessionNumber =
			simulatorSessionNumberHelper.find (
				number.getId ());

		if (
			optionalIsPresent (
				existingSimulatorSessionNumber)
		) {
			return existingSimulatorSessionNumber.get ();
		}

		// create new

		SimulatorSessionNumberRec newSimulatorSessionNumber =
			simulatorSessionNumberHelper.insert (
				simulatorSessionNumberHelper.createInstance ()

			.setNumber (
				number)

		);

		return newSimulatorSessionNumber;

	}

}