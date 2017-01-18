package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

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

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Object searchObject) {

		ManualResponderNumberSearch search =
			genericCastUnchecked (
				searchObject);

		search

			.manualResponderId (
				requestContext.stuffIntegerRequired (
					"manualResponderId"));

	}

}
