package wbs.test.simulator.console;

import static wbs.framework.utils.etc.Misc.toInteger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.text.web.TextResponder;
import wbs.test.simulator.model.SimEventObjectHelper;
import wbs.test.simulator.model.SimEventRec;

import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("simulatorPollAction")
public
class SimulatorPollAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	SimEventObjectHelper simEventHelper;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	Provider<TextResponder> textResponder;

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal () {

		int lastId =
			toInteger (requestContext.getForm ("last"));

		int limit =
			toInteger (requestContext.getForm ("limit"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<SimEventRec> events =
			simEventHelper.findAfterLimit (
				lastId,
				limit);

		// create response

		List<Object> ret =
			new ArrayList<Object> ();

		for (SimEventRec event : events) {

			Object eventData =
				JSONValue.parse (event.getData ());

			ret.add (
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
						eventData)

					.build ());

		}

		// return it

		String responseText =
			JSONValue.toJSONString (ret);

		return textResponder.get ()
			.text (responseText);

	}

}
