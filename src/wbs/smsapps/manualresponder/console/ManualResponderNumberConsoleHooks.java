package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.event.logic.EventLogic;

import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberSearch;

import wbs.utils.random.RandomLogic;

@SingletonComponent ("manualResponderNumberConsoleHooks")
public
class ManualResponderNumberConsoleHooks
	implements ConsoleHooks <ManualResponderNumberRec> {

	// singleton dependencies

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Transaction parentTransaction,
			@NonNull Object searchObject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"applySearchFilter");

		) {

			ManualResponderNumberSearch search =
				genericCastUnchecked (
					searchObject);

			search

				.manualResponderId (
					requestContext.stuffIntegerRequired (
						"manualResponderId"));

		}

	}

}
