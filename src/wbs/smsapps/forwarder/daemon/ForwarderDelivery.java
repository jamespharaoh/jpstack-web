package wbs.smsapps.forwarder.daemon;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;

import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportRec;

@PrototypeComponent ("forwarderDelivery")
public
class ForwarderDelivery
	implements DeliveryHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@SingletonDependency
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@SingletonDependency
	ForwarderMessageOutReportObjectHelper forwarderMessageOutReportHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsOutboxLogic outboxLogic;

	// details

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"forwarder");

	}

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ForwarderDelivery.handle (deliveryId, ref)",
				this);

		// get the delivery

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		// lookup the forwarder message out

		ForwarderMessageOutRec forwarderMessageOut =
			forwarderMessageOutHelper.findRequired (
				delivery.getMessage ().getRef ());

		// unhold / cancel next message(s) if required

		if (
			forwarderMessageOut.getNextForwarderMessageOut () != null
			&& forwarderMessageOut
				.getNextForwarderMessageOut ()
				.getMessage ()
				.getStatus ()
					== MessageStatus.held) {

			if (delivery.getNewMessageStatus ().isGoodType ()) {

				outboxLogic.unholdMessage (
					taskLogger,
					forwarderMessageOut
						.getNextForwarderMessageOut ()
						.getMessage ());

			}

			if (delivery.getNewMessageStatus ().isBadType ()) {

				for (

					ForwarderMessageOutRec nextForwarderMessageOut =
						forwarderMessageOut.getNextForwarderMessageOut ();

					nextForwarderMessageOut != null;

					nextForwarderMessageOut =
						nextForwarderMessageOut.getNextForwarderMessageOut ()

				) {

					outboxLogic.cancelMessage (
						taskLogger,
						nextForwarderMessageOut.getMessage ());

				}

			}

		}

		// do reports if required

		if (forwarderMessageOut.getForwarder ().getReportEnabled ()) {

			// create the forwarder message out report

			ForwarderMessageOutReportRec forwarderMessageOutReport =
				forwarderMessageOutReportHelper.insert (
					taskLogger,
					forwarderMessageOutReportHelper.createInstance ()

				.setForwarderMessageOut (
					forwarderMessageOut)

				.setIndex (
					forwarderMessageOut.getReportIndexNext ())

				.setOldMessageStatus (
					delivery.getOldMessageStatus ())

				.setNewMessageStatus (
					delivery.getNewMessageStatus ())

				.setCreatedTime (
					transaction.now ())

			);

			// update the forwarder message out

			if (forwarderMessageOut.getReportIndexPending () == null) {

				forwarderMessageOut

					.setReportIndexPending (
						forwarderMessageOutReport.getIndex ())

					.setReportRetryTime (
						transaction.now ())

					.setReportTries (
						0l);

			}

			forwarderMessageOut.setReportIndexNext (
				forwarderMessageOut.getReportIndexNext () + 1);

		}

		// remove the dnq

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
