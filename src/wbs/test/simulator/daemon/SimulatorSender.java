package wbs.test.simulator.daemon;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;

import org.json.simple.JSONValue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorEventRec;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelper;
import wbs.test.simulator.model.SimulatorSessionNumberRec;
import wbs.test.simulator.model.SimulatorSessionObjectHelper;

@SingletonComponent ("simulatorSender")
public
class SimulatorSender
	extends AbstractSmsSender1<Integer> {

	// dependencies

	@Inject
	Database database;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	SimulatorEventObjectHelper simulatorEventHelper;

	@Inject
	SimulatorSessionObjectHelper simulatorSessionHelper;

	@Inject
	SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper;

	// details

	@Override
	protected
	String getSenderCode () {
		return "simulator";
	}

	@Override
	protected
	String getThreadName () {
		return "SimSender";
	}

	// implementation

	@Override
	protected
	Integer getMessage (
			OutboxRec outbox) {

		return outbox.getId ();

	}

	@Override
	protected
	Optional<List<String>> sendMessage (
			@NonNull Integer messageId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SimulatorSender.sendMessage (messageId)",
				this);

		MessageRec message =
			messageHelper.findRequired (
				messageId);

		RouteRec route =
			message.getRoute ();

		NetworkRec network =
			message.getNetwork ();

		// create event data

		Object data =
			ImmutableMap.<String,Object>builder ()

			.put (
				"message",
				ImmutableMap.<String,Object>builder ()
					.put ("id", message.getId ())
					.put ("numFrom", message.getNumFrom ())
					.put ("numTo", message.getNumTo ())
					.put ("text", message.getText ().getText ())
					.build ())

			.put (
				"route",
				ImmutableMap.<String,Object>builder ()
					.put ("id", route.getId ())
					.put ("code", route.getCode ())
					.put ("outCharge", route.getOutCharge ())
					.build ())

			.put (
				"network",
				ImmutableMap.<String,Object>builder ()
					.put ("id", network.getId ())
					.put ("code", network.getCode ())
					.build ())

			.build ();

		// lookup session

		SimulatorSessionNumberRec simulatorSessionNumber =
			simulatorSessionNumberHelper.findOrThrow (
				message.getNumber ().getId (),
				() -> permFailure (
					"No session for number"));

		// create event

		SimulatorEventRec event =
			simulatorEventHelper.insert (
				simulatorEventHelper.createInstance ()

			.setSimulatorSession (
				simulatorSessionNumber.getSimulatorSession ())

			.setType (
				"message_out")

			.setTimestamp (
				transaction.now ())

			.setData (
				JSONValue.toJSONString (data)));


		// finish up

		transaction.commit ();

		return Optional.of (
			ImmutableList.of (
				Integer.toString (
					event.getId ())));

	}

	// data structures

	static
	class Work {
		OutboxRec outbox;
		MessageRec message;
	}

}
