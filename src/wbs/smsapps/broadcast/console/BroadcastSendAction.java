package wbs.smsapps.broadcast.console;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

@PrototypeComponent ("broadcastSendAction")
public
class BroadcastSendAction
	extends ConsoleAction {

	// dependencies

	@Inject
	BroadcastConsoleHelper broadcastHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {
		return responder ("broadcastSendResponder");
	}

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		BroadcastRec broadcast =
			broadcastHelper.find (
				requestContext.stuffInt ("broadcastId"));

		BroadcastConfigRec broadcastConfig =
			broadcast.getBroadcastConfig ();

		if (requestContext.parameter ("send") != null) {

			if (broadcast.getState () != BroadcastState.unsent) {

				requestContext.addError (
					stringFormat (
						"Cannot send broadcast in state %s",
						broadcast.getState ()));

				return null;

			}

			broadcast

				.setSentUser (
					userConsoleLogic.userRequired ())

				.setScheduledTime (
					transaction.now ())

				.setState (
					BroadcastState.sending);

			broadcastConfig

				.setNumUnsent (
					broadcastConfig.getNumUnsent () - 1)

				.setNumSending (
					broadcastConfig.getNumSending () + 1);

			eventLogic.createEvent (
				"broadcast_scheduled",
				userConsoleLogic.userRequired (),
				broadcast,
				transaction.now ());

			eventLogic.createEvent (
				"broadcast_send_begun",
				broadcast);

			transaction.commit ();

			requestContext.addNotice (
				"Broadcast is now sending");

			return null;

		}

		if (requestContext.parameter ("schedule") != null) {

			if (broadcast.getState () != BroadcastState.unsent) {

				requestContext.addError (
					stringFormat (
						"Cannot send broadcast in state %s",
						broadcast.getState ()));

				return null;

			}

			Instant scheduledTime;

			try {

				scheduledTime =
					timeFormatter.timestampStringToInstant (
						userConsoleLogic.timezone (),
						requestContext.parameter ("timestamp"));

			} catch (Exception exception) {

				requestContext.addError (
					"The timestamp is not valid");

				return null;

			}

			broadcast

				.setSentUser (
					userConsoleLogic.userRequired ())

				.setScheduledTime (
					scheduledTime)

				.setState (
					BroadcastState.scheduled);

			broadcastConfig

				.setNumUnsent (
					broadcastConfig.getNumUnsent () - 1)

				.setNumScheduled (
					broadcastConfig.getNumScheduled () + 1);

			eventLogic.createEvent (
				"broadcast_scheduled",
				userConsoleLogic.userRequired (),
				broadcast,
				scheduledTime);

			transaction.commit ();

			requestContext.addNotice (
				"Broadcast is now scheduled");

			return null;

		}

		if (requestContext.parameter ("unschedule") != null) {

			if (broadcast.getState () != BroadcastState.scheduled) {

				requestContext.addError (
					stringFormat (
						"Cannot unschedule broadcast in state %s",
						broadcast.getState ()));

				return null;

			}

			broadcast
				.setSentUser (null)
				.setScheduledTime (null)
				.setState (BroadcastState.unsent);

			broadcastConfig
				.setNumScheduled (
					broadcastConfig.getNumScheduled () - 1)
				.setNumUnsent (
					broadcastConfig.getNumUnsent () + 1);

			eventLogic.createEvent (
				"broadcast_unscheduled",
				userConsoleLogic.userRequired (),
				broadcast);

			transaction.commit ();

			requestContext.addNotice (
				"Broadcast is now unscheduled");

			return null;

		}

		if (requestContext.parameter ("cancel") != null) {

			if (broadcast.getState ()
					== BroadcastState.partiallySent) {

				requestContext.addWarning (
					"Already partially sent and cancelled");

				return null;

			}

			if (broadcast.getState ()
					== BroadcastState.cancelled) {

				requestContext.addNotice (
					"Broadcast is already cancelled");

				return null;

			}

			if (in (
					broadcast.getState (),
					BroadcastState.unsent,
					BroadcastState.scheduled)) {

				broadcast.setState (
					BroadcastState.cancelled);

				eventLogic.createEvent (
					"broadcast_cancelled",
					userConsoleLogic.userRequired (),
					broadcast);

				transaction.commit ();

				requestContext.addNotice (
					"Broadcast cancelled");

				return null;

			}

			if (in (
					broadcast.getState (),
					BroadcastState.sending)) {

				broadcast.setState (
					BroadcastState.partiallySent);

				eventLogic.createEvent (
					"broadcast_cancelled",
					userConsoleLogic.userRequired (),
					broadcast);

				transaction.commit ();

				requestContext.addNotice (
					"Broadcast cancelled after being partially sent");

				return null;

			}

		}

		throw new RuntimeException ();

	}

}
