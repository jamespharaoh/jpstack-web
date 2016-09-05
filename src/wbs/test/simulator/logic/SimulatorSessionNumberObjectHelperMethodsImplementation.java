package wbs.test.simulator.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import wbs.sms.number.core.model.NumberRec;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelper;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelperMethods;
import wbs.test.simulator.model.SimulatorSessionNumberRec;

public
class SimulatorSessionNumberObjectHelperMethodsImplementation
	implements SimulatorSessionNumberObjectHelperMethods {

	// indirect dependencies

	@Inject
	Provider <SimulatorSessionNumberObjectHelper>
	simulatorSessionNumberHelperProvider;

	// implementation

	@Override
	public
	SimulatorSessionNumberRec findOrCreate (
			@NonNull NumberRec number) {

		SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper =
			simulatorSessionNumberHelperProvider.get ();

		// find existing

		Optional<SimulatorSessionNumberRec> existingSimulatorSessionNumber =
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