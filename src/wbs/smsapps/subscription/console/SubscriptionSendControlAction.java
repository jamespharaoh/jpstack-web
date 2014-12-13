package wbs.smsapps.subscription.console;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;

@PrototypeComponent ("subscriptionSendControlAction")
public
class SubscriptionSendControlAction
	extends ConsoleAction {

	// dependencies

	@Inject
	SubscriptionSendConsoleHelper subscriptionSendHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"subscriptionSendControlResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		SubscriptionSendRec subscriptionSend =
			subscriptionSendHelper.find (
				requestContext.stuffInt (
					"subscriptionSendId"));

		if (
			requestContext.parameter ("send")
				!= null
		) {

			if (
				subscriptionSend.getState ()
					!= SubscriptionSendState.notSent
			) {

				requestContext.addError (
					stringFormat (
						"Cannot send subscription send in state %s",
						subscriptionSend.getState ()));

				return null;

			}

			subscriptionSend

				.setSentUser (
					myUser)

				.setScheduledTime (
					transaction.now ())

				.setScheduledForTime (
					transaction.now ())

				.setSentTime (
					transaction.now ())

				.setState (
					SubscriptionSendState.sending);

			eventLogic.createEvent (
				"subscription_send_scheduled",
				myUser,
				subscriptionSend,
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					transaction.now ()));

			eventLogic.createEvent (
				"subscription_send_begun",
				subscriptionSend);

			transaction.commit ();

			requestContext.addNotice (
				"Subscription send is now sending");

			return null;

		}

		if (
			requestContext.parameter ("schedule") != null
		) {

			if (
				subscriptionSend.getState ()
					!= SubscriptionSendState.notSent
			) {

				requestContext.addError (
					stringFormat (
						"Cannot send subscription send in state %s",
						subscriptionSend.getState ()));

				return null;

			}

			Instant scheduledTime;

			try {

				scheduledTime =
					timeFormatter.timestampStringToInstant (
						timeFormatter.defaultTimezone (),
						requestContext.parameter ("timestamp"));

			} catch (Exception exception) {

				requestContext.addError (
					"The timestamp is not valid");

				return null;

			}

			subscriptionSend

				.setSentUser (
					myUser)

				.setScheduledTime (
					transaction.now ())

				.setScheduledForTime (
					scheduledTime)

				.setState (
					SubscriptionSendState.scheduled);

			eventLogic.createEvent (
				"subscription_send_scheduled",
				myUser,
				subscriptionSend,
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					scheduledTime));

			transaction.commit ();

			requestContext.addNotice (
				"Subscription send is now scheduled");

			return null;

		}

		if (
			requestContext.parameter ("unschedule")
				!= null
		) {

			if (
				subscriptionSend.getState ()
					!= SubscriptionSendState.scheduled
			) {

				requestContext.addError (
					stringFormat (
						"Cannot unschedule subscription send in state %s",
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
				"subscription_send_unscheduled",
				myUser,
				subscriptionSend);

			transaction.commit ();

			requestContext.addNotice (
				"Subscription send is now unscheduled");

			return null;

		}

		if (
			requestContext.parameter ("cancel")
				!= null
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
				in (
					subscriptionSend.getState (),
					SubscriptionSendState.notSent,
					SubscriptionSendState.scheduled)
			) {

				subscriptionSend

					.setState (
						SubscriptionSendState.cancelled);

				eventLogic.createEvent (
					"subscription_send_cancelled",
					myUser,
					subscriptionSend);

				transaction.commit ();

				requestContext.addNotice (
					"Subscription send cancelled");

				return null;

			}

			if (
				in (
					subscriptionSend.getState (),
					SubscriptionSendState.sending)
			) {

				subscriptionSend

					.setState (
						SubscriptionSendState.partiallySent);

				eventLogic.createEvent (
					"subscription_send_cancelled",
					myUser,
					subscriptionSend);

				transaction.commit ();

				requestContext.addNotice (
					"Subscription send cancelled after being partially sent");

				return null;

			}

		}

		throw new RuntimeException ();

	}

}
