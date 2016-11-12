package wbs.test.simulator.console;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import org.json.simple.JSONValue;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorEventRec;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("simulatorSessionPollAction")
public
class SimulatorSessionPollAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SimulatorEventObjectHelper simulatorEventHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		Long lastId =
			parseIntegerRequired (
				requestContext.formRequired (
					"last"));

		Long limit =
			parseIntegerRequired (
				requestContext.formRequired (
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
