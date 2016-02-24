package wbs.smsapps.manualresponder.console;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.console.helper.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.RandomLogic;
import wbs.platform.event.logic.EventLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberSearch;

@SingletonComponent ("manualResponderNumberConsoleHooks")
public
class ManualResponderNumberConsoleHooks
	implements ConsoleHooks<ManualResponderNumberRec> {

	// dependencies

	@Inject
	EventLogic eventLogic;

	@Inject
	RandomLogic randomLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Object searchObject) {

		ManualResponderNumberSearch search =
			(ManualResponderNumberSearch)
			searchObject;

		search

			.manualResponderId (
				requestContext.stuffInt (
					"manualResponderId"));

	}

}
