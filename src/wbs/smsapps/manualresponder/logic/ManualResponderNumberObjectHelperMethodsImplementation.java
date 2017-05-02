package wbs.smsapps.manualresponder.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelperMethods;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

import wbs.utils.random.RandomLogic;

public
class ManualResponderNumberObjectHelperMethodsImplementation
	implements ManualResponderNumberObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	// implementation

	@Override
	public
	ManualResponderNumberRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			// find or create number

			ManualResponderNumberRec manualResponderNumber =
				manualResponderNumberHelper.find (
					transaction,
					manualResponder,
					number);

			if (manualResponderNumber == null) {

				manualResponderNumber =
					manualResponderNumberHelper.insert (
						transaction,
						manualResponderNumberHelper.createInstance ()

					.setManualResponder (
						manualResponder)

					.setNumber (
						number)

					.setCode (
						randomLogic.generateNumericNoZero (
							8))

				);

			}

			return manualResponderNumber;

		}

	}

}
