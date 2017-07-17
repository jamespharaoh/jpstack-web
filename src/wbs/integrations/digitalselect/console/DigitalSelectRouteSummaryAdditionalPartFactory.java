package wbs.integrations.digitalselect.console;

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

@SingletonComponent ("digitalSelectRouteSummaryAdditionalPartFactory")
public
class DigitalSelectRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <DigitalSelectRouteSummaryAdditionalPart>
		digitalSelectRouteSummaryAdditionalPartProvider;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"digital_select"
		};

	}

	// simplementation

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

			return digitalSelectRouteSummaryAdditionalPartProvider.provide (
				transaction);

		}

	}

}
