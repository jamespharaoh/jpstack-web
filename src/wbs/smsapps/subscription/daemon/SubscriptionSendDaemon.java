package wbs.smsapps.subscription.daemon;

import java.util.Date;

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
				subscriptionSendHelper.findDue ();

			if (send == null)
				return;

			send
				.setStatus (SubscriptionStatus.sentAutomatically)
				.setSentTime (new Date ());

			subscriptionUtils.subscriptionSend (send);

			transaction.commit();

		}

	}

}
