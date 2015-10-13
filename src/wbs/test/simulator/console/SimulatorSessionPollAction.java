package wbs.test.simulator.console;

import static wbs.framework.utils.etc.Misc.toInteger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONValue;

import wbs.console.action.ConsoleAction;
import wbs.console.misc.TimeFormatter;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.Responder;
import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorEventRec;

import com.google.common.collect.ImmutableMap;

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
	TimeFormatter timeFormatter;

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

		int lastId =
			toInteger (requestContext.getForm ("last"));

		int limit =
			toInteger (requestContext.getForm ("limit"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<SimulatorEventRec> events =
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
					timeFormatter.instantToDateStringShort (
						timeFormatter.defaultTimezone (),
						event.getTimestamp ()))

				.put (
					"time",
					timeFormatter.instantToTimeString (
						timeFormatter.defaultTimezone (),
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
