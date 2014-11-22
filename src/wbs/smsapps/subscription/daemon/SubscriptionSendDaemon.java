package wbs.smsapps.subscription.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionSendObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionStatus;

@SingletonComponent ("subscriptionSendDaemon")
public
class SubscriptionSendDaemon
	extends AbstractDaemonService {

	@Inject
	Database database;

	@Inject
	SubscriptionLogic subscriptionUtils;

	@Inject
	SubscriptionSendObjectHelper subscriptionSendHelper;

	int sleepSeconds = 60;

	@Override
	protected
	String getThreadName () {
		return "SubscriptionSend";
	}

	@Override
	protected
	void runService () {

		while (true) {

			checkSubscriptions ();

			try {

				Thread.sleep (
					sleepSeconds * 1000);

			} catch (InterruptedException exception) {

				return;

			}

		}

	}

	private
	void checkSubscriptions () {

		for (;;) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			SubscriptionSendRec send =
				subscriptionSendHelper.findDue (
					transaction.now ());

			if (send == null)
				return;

			send

				.setStatus (
					SubscriptionStatus.sentAutomatically)

				.setSentTime (
					instantToDate (
						transaction.now ()));

			subscriptionUtils.subscriptionSend (
				send);

			transaction.commit ();

		}

	}

}
