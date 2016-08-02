package wbs.smsapps.forwarder.daemon;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;

import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@Inject
	ForwarderMessageOutReportObjectHelper forwarderMessageOutReportHelper;

	@Inject
	SmsOutboxLogic outboxLogic;

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"forwarder");

	}

	@Override
	public
	void handle (
			int deliveryId,
			Long ref) {

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
						nextForwarderMessageOut.getMessage ());

				}

			}

		}

		// do reports if required

		if (forwarderMessageOut.getForwarder ().getReportEnabled ()) {

			// create the forwarder message out report

			ForwarderMessageOutReportRec forwarderMessageOutReport =
				forwarderMessageOutReportHelper.insert (
					forwarderMessageOutReportHelper.createInstance ()

				.setForwarderMessageOut (
					forwarderMessageOut)

				.setIndex (
					(int) (long)
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
						(long)
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
