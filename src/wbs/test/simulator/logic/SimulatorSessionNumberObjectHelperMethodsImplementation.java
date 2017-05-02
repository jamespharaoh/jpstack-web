package wbs.test.simulator.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

import wbs.test.simulator.model.SimulatorSessionNumberObjectHelper;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelperMethods;
import wbs.test.simulator.model.SimulatorSessionNumberRec;

public
class SimulatorSessionNumberObjectHelperMethodsImplementation
	implements SimulatorSessionNumberObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper;

	// implementation

	@Override
	public
	SimulatorSessionNumberRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			// find existing

			Optional <SimulatorSessionNumberRec>
				existingSimulatorSessionNumber =
					simulatorSessionNumberHelper.find (
						transaction,
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
					transaction,
					simulatorSessionNumberHelper.createInstance ()

				.setNumber (
					number)

			);

			return newSimulatorSessionNumber;

		}

	}

}