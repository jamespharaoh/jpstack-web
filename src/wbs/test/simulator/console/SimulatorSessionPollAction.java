package wbs.test.simulator.console;

import static wbs.framework.utils.etc.NumberUtils.parseLongRequired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.json.simple.JSONValue;

import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorEventRec;

@PrototypeComponent ("simulatorSessionPollAction")
public
class SimulatorSessionPollAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	SimulatorEventObjectHelper simulatorEventHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	// implementation

	@Override
	protected
	Responder goReal () {

		Long lastId =
			parseLongRequired (
				requestContext.getForm (
					"last"));

		Long limit =
			parseLongRequired (
				requestContext.getForm (
					"limit"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"SimulatorSessionPollAction.goReal ()",
				this);

		List <SimulatorEventRec> events =
			simulatorEventHelper.findAfterLimit (
				lastId,
				limit);

		// create response

		Map<String,Object> responseObject =
			new LinkedHashMap<String,Object> ();

		List<Object> eventResponses =
			new ArrayList<Object> ();

		for (
			SimulatorEventRec event
				: events
		) {

			eventResponses.add (
				ImmutableMap.<String,Object>builder ()

				.put (
					"id",
					event.getId ())

				.put (
					"date",
					userConsoleLogic.dateStringShort (
						event.getTimestamp ()))

				.put (
					"time",
					userConsoleLogic.timeString (
						event.getTimestamp ()))

				.put (
					"type",
					event.getType ())

				.put (
					"data",
					JSONValue.parse (
						event.getData ()))

				.build ()

			);

		}

		responseObject.put (
			"events",
			eventResponses);

		// return it

		return jsonResponderProvider.get ()

			.value (
				responseObject);

	}

}
