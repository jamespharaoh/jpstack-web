package wbs.smsapps.subscription.console;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;

import wbs.web.responder.Responder;

@PrototypeComponent ("subscriptionSendControlAction")
public
class SubscriptionSendControlAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SubscriptionLogic subscriptionLogic;

	@SingletonDependency
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@SingletonDependency
	SubscriptionSendConsoleHelper subscriptionSendHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"subscriptionSendControlResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			SubscriptionSendRec subscriptionSend =
				subscriptionSendHelper.findFromContextRequired (
					transaction);

			if (
				optionalIsPresent (
					requestContext.parameter (
						"send"))
			) {

				if (
					subscriptionSend.getState ()
						!= SubscriptionSendState.notSent
				) {

					requestContext.addErrorFormat (
						"Cannot send subscription send in state \"%s\"",
						enumNameSpaces (
							subscriptionSend.getState ()));

					return null;

				}

				subscriptionLogic.scheduleSend (
					transaction,
					subscriptionSend,
					transaction.now (),
					userConsoleLogic.userRequired (
						transaction));

				transaction.commit ();

				requestContext.addNotice (
					"Subscription send scheduled for now");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"schedule"))
			) {

				if (
					subscriptionSend.getState ()
						!= SubscriptionSendState.notSent
				) {

					requestContext.addErrorFormat (
						"Cannot send subscription send in state \"%s\"",
						enumNameSpaces (
							subscriptionSend.getState ()));

					return null;

				}

				Instant scheduledTime;

				try {

					scheduledTime =
						userConsoleLogic.timestampStringToInstant (
							transaction,
							requestContext.parameterRequired (
								"timestamp"));

				} catch (Exception exception) {

					requestContext.addError (
						"The timestamp is not valid");

					return null;

				}

				subscriptionLogic.scheduleSend (
					transaction,
					subscriptionSend,
					scheduledTime,
					userConsoleLogic.userRequired (
						transaction));

				transaction.commit ();

				requestContext.addNotice (
					"Subscription send is now scheduled");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"unschedule"))
			) {

				if (
					subscriptionSend.getState ()
						!= SubscriptionSendState.scheduled
				) {

					requestContext.addErrorFormat (
						"Cannot unschedule subscription send in state %s",
						enumNameSpaces (
							subscriptionSend.getState ()));

					return null;

				}

				subscriptionSend

					.setSentUser (
						null)

					.setScheduledTime (
						null)

					.setScheduledForTime (
						null)

					.setState (
						SubscriptionSendState.notSent);

				eventLogic.createEvent (
					transaction,
					"subscription_send_unscheduled",
					userConsoleLogic.userRequired (
						transaction),
					subscriptionSend);

				transaction.commit ();

				requestContext.addNotice (
					"Subscription send is now unscheduled");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"cancel"))
			) {

				if (
					subscriptionSend.getState ()
						== SubscriptionSendState.partiallySent
				) {

					requestContext.addWarning (
						"Already partially sent and cancelled");

					return null;

				}

				if (
					subscriptionSend.getState ()
						== SubscriptionSendState.cancelled
				) {

					requestContext.addNotice (
						"Subscription send is already cancelled");

					return null;

				}

				if (
					enumInSafe (
						subscriptionSend.getState (),
						SubscriptionSendState.notSent,
						SubscriptionSendState.scheduled)
				) {

					subscriptionSend

						.setState (
							SubscriptionSendState.cancelled);

					eventLogic.createEvent (
						transaction,
						"subscription_send_cancelled",
						userConsoleLogic.userRequired (
							transaction),
						subscriptionSend);

					transaction.commit ();

					requestContext.addNotice (
						"Subscription send cancelled");

					return null;

				}

				if (
					enumInSafe (
						subscriptionSend.getState (),
						SubscriptionSendState.sending)
				) {

					subscriptionSend

						.setState (
							SubscriptionSendState.partiallySent);

					eventLogic.createEvent (
						transaction,
						"subscription_send_cancelled",
						userConsoleLogic.userRequired (
							transaction),
						subscriptionSend);

					transaction.commit ();

					requestContext.addNoticeFormat (
						"Subscription send cancelled after being partially ",
						"sent");

					return null;

				}

			}

			throw new RuntimeException ();

		}

	}

}
