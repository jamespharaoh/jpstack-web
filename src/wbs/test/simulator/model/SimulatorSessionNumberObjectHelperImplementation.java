package wbs.test.simulator.model;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.sms.number.core.model.NumberRec;

public
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