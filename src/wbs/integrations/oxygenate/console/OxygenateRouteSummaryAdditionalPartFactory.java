package wbs.integrations.oxygenate.console;

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

@SingletonComponent ("oxygenateRouteSummaryAdditionalPartFactory")
public
class OxygenateRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <OxygenateRouteSummaryAdditionalPart>
		oxygen8RouteSummaryAdditionalPartProvider;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"oxygen8"
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

			return oxygen8RouteSummaryAdditionalPartProvider.provide (
				transaction);

		}

	}

}
