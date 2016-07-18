package wbs.test.simulator.model;

import static wbs.framework.utils.etc.Misc.isPresent;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;

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
			@NonNull NumberRec number) {

		SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper =
			simulatorSessionNumberHelperProvider.get ();

		// find existing

		Optional<SimulatorSessionNumberRec> existingSimulatorSessionNumber =
			simulatorSessionNumberHelper.find (
				number.getId ());

		if (
			isPresent (
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