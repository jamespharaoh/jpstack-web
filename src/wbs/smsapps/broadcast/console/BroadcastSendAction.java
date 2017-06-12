package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

import wbs.utils.time.TimeFormatter;

import wbs.web.exceptions.HttpBadRequestException;
import wbs.web.responder.WebResponder;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("broadcastSendResponder")
	Provider <WebResponder> sendResponderProvider;

	// details

	@Override
	protected
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return sendResponderProvider.get ();

	}

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			BroadcastRec broadcast =
				broadcastHelper.findFromContextRequired (
					transaction);

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
						userConsoleLogic.userRequired (
							transaction))

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
					transaction,
					"broadcast_scheduled",
					userConsoleLogic.userRequired (
						transaction),
					broadcast,
					transaction.now ());

				eventLogic.createEvent (
					transaction,
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
							userConsoleLogic.timezone (
								transaction),
							requestContext.parameterRequired (
								"timestamp"));

				} catch (Exception exception) {

					requestContext.addError (
						"The timestamp is not valid");

					return null;

				}

				broadcast

					.setSentUser (
						userConsoleLogic.userRequired (
							transaction))

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
					transaction,
					"broadcast_scheduled",
					userConsoleLogic.userRequired (
						transaction),
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
					transaction,
					"broadcast_unscheduled",
					userConsoleLogic.userRequired (
						transaction),
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
						transaction,
						"broadcast_cancelled",
						userConsoleLogic.userRequired (
							transaction),
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
						transaction,
						"broadcast_cancelled",
						userConsoleLogic.userRequired (
							transaction),
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
