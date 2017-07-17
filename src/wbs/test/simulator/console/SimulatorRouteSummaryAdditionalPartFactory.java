package wbs.test.simulator.console;

import lombok.NonNull;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("simulatorRouteSummaryAdditionalPartFactory")
public
class SimulatorRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// prototype dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@PrototypeDependency
	ComponentProvider <SimulatorRouteSummaryAdditionalPart>
		simulatorRouteSummaryAdditionalPartProvider;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"simulator"
		};

	}

	// implementation

	@Override
	public
	PagePart getPagePart (
			@NonNull Transaction parentTransaction,
			@NonNull String senderCode) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getPagePart");

		) {

			return simulatorRouteSummaryAdditionalPartProvider.provide (
				transaction);

		}

	}

}
