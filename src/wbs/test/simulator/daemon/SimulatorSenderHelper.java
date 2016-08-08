package wbs.test.simulator.daemon;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.test.simulator.daemon.SimulatorSenderHelper.State;
import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorEventRec;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelper;
import wbs.test.simulator.model.SimulatorSessionNumberRec;
import wbs.test.simulator.model.SimulatorSessionObjectHelper;

@SingletonComponent ("simulatorSender")
public
class SimulatorSenderHelper
	implements SmsSenderHelper<State> {

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
	public
	String senderCode () {
		return "simulator";
	}

	// implementation

	@Override
	public
	SetupRequestResult<State> setupRequest (
			@NonNull OutboxRec outbox) {

		return new SetupRequestResult<State> ()

			.status (
				SetupRequestStatus.success)

			.state (
				new State ()

				.messageId (
					(long) (int) outbox.getId ())

			);

	}

	@Override
	public
	PerformSendResult performSend (
			@NonNull State state) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SimulatorSender.sendMessage (messageId)",
				this);

		MessageRec message =
			messageHelper.findRequired (
				state.messageId);

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

		Optional<SimulatorSessionNumberRec> simulatorSessionNumberOptional =
			simulatorSessionNumberHelper.find (
				message.getNumber ().getId ());

		if (
			isNotPresent (
				simulatorSessionNumberOptional)
		) {

			return new PerformSendResult ()

				.status (
					PerformSendStatus.remoteError)

				.statusMessage (
					"No session for number");

		}

		SimulatorSessionNumberRec simulatorSessionNumber =
			simulatorSessionNumberOptional.get ();

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

		state.otherIds (
			ImmutableList.of (
				Integer.toString (
					event.getId ())));

		return new PerformSendResult ()

			.status (
				PerformSendStatus.success)

			.statusMessage (
				"No session for number");

	}

	@Override
	public
	ProcessResponseResult processSend (
			@NonNull State state) {

		return new ProcessResponseResult ()

			.status (
				ProcessResponseStatus.success)

			.otherIds (
				state.otherIds);

	}

	@Accessors (fluent = true)
	@Data
	public static
	class State {

		Long messageId;
		List<String> otherIds;

	}

}
