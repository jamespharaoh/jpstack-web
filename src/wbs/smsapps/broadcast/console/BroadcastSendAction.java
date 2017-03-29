package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

import wbs.utils.time.TimeFormatter;

import wbs.web.exceptions.HttpBadRequestException;
import wbs.web.responder.Responder;

@PrototypeComponent ("broadcastSendAction")
public
class BroadcastSendAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	BroadcastConsoleHelper broadcastHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {
		return responder ("broadcastSendResponder");
	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		try (

			Transaction transaction =
				database.beginReadWrite (
					"BroadcastSendAction.goReal ()",
					this);

		) {

			BroadcastRec broadcast =
				broadcastHelper.findFromContextRequired ();

			BroadcastConfigRec broadcastConfig =
				broadcast.getBroadcastConfig ();

			if (
				optionalIsPresent (
					requestContext.parameter (
						"send"))
			) {

				if (
					enumNotEqualSafe (
						broadcast.getState (),
						BroadcastState.unsent)
				) {

					requestContext.addErrorFormat (
						"Cannot send broadcast in state \"%s\"",
						enumNameSpaces (
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
					taskLogger,
					"broadcast_scheduled",
					userConsoleLogic.userRequired (),
					broadcast,
					transaction.now ());

				eventLogic.createEvent (
					taskLogger,
					"broadcast_send_begun",
					broadcast);

				transaction.commit ();

				requestContext.addNotice (
					"Broadcast is now sending");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"schedule"))
			) {

				if (broadcast.getState () != BroadcastState.unsent) {

					requestContext.addErrorFormat (
						"Cannot send broadcast in state \"%s\"",
						enumNameSpaces (
							broadcast.getState ()));

					return null;

				}

				Instant scheduledTime;

				try {

					scheduledTime =
						timeFormatter.timestampStringToInstant (
							userConsoleLogic.timezone (),
							requestContext.parameterRequired (
								"timestamp"));

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
					taskLogger,
					"broadcast_scheduled",
					userConsoleLogic.userRequired (),
					broadcast,
					scheduledTime);

				transaction.commit ();

				requestContext.addNotice (
					"Broadcast is now scheduled");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"unschedule"))
			) {

				if (broadcast.getState () != BroadcastState.scheduled) {

					requestContext.addErrorFormat (
						"Cannot unschedule broadcast in state \"%s\"",
						enumNameSpaces (
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
					taskLogger,
					"broadcast_unscheduled",
					userConsoleLogic.userRequired (),
					broadcast);

				transaction.commit ();

				requestContext.addNotice (
					"Broadcast is now unscheduled");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"cancel"))
			) {

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

				if (
					enumInSafe (
						broadcast.getState (),
						BroadcastState.unsent,
						BroadcastState.scheduled)
				) {

					broadcast.setState (
						BroadcastState.cancelled);

					eventLogic.createEvent (
						taskLogger,
						"broadcast_cancelled",
						userConsoleLogic.userRequired (),
						broadcast);

					transaction.commit ();

					requestContext.addNotice (
						"Broadcast cancelled");

					return null;

				}

				if (
					enumInSafe (
						broadcast.getState (),
						BroadcastState.sending)
				) {

					broadcast.setState (
						BroadcastState.partiallySent);

					eventLogic.createEvent (
						taskLogger,
						"broadcast_cancelled",
						userConsoleLogic.userRequired (),
						broadcast);

					transaction.commit ();

					requestContext.addNotice (
						"Broadcast cancelled after being partially sent");

					return null;

				}

			}

			throw new HttpBadRequestException ();

		}

	}

}
