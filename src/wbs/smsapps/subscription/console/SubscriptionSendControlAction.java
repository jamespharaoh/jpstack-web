package wbs.smsapps.subscription.console;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import org.joda.time.Instant;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;

@PrototypeComponent ("subscriptionSendControlAction")
public
class SubscriptionSendControlAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	SubscriptionLogic subscriptionLogic;

	@Inject
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@Inject
	SubscriptionSendConsoleHelper subscriptionSendHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

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
			database.beginReadWrite (
				"SubscriptionSendControlAction.goReal ()",
				this);

		SubscriptionSendRec subscriptionSend =
			subscriptionSendHelper.findRequired (
				requestContext.stuffInteger (
					"subscriptionSendId"));

		if (
			isPresent (
				requestContext.parameter (
					"send"))
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

			subscriptionLogic.scheduleSend (
				subscriptionSend,
				transaction.now (),
				userConsoleLogic.userRequired ());

			transaction.commit ();

			requestContext.addNotice (
				"Subscription send scheduled for now");

			return null;

		}

		if (
			isPresent (
				requestContext.parameter (
					"schedule"))
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
					userConsoleLogic.timestampStringToInstant (
						requestContext.parameterRequired (
							"timestamp"));

			} catch (Exception exception) {

				requestContext.addError (
					"The timestamp is not valid");

				return null;

			}

			subscriptionLogic.scheduleSend (
				subscriptionSend,
				scheduledTime,
				userConsoleLogic.userRequired ());

			transaction.commit ();

			requestContext.addNotice (
				"Subscription send is now scheduled");

			return null;

		}

		if (
			isPresent (
				requestContext.parameter (
					"unschedule"))
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
				userConsoleLogic.userRequired (),
				subscriptionSend);

			transaction.commit ();

			requestContext.addNotice (
				"Subscription send is now unscheduled");

			return null;

		}

		if (
			isPresent (
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
					userConsoleLogic.userRequired (),
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
					userConsoleLogic.userRequired (),
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
