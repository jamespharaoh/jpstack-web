package wbs.smsapps.forwarder.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.OutboxLogic;
import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportRec;

import com.google.common.collect.ImmutableList;

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
	OutboxLogic outboxLogic;

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
			Integer ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get the delivery

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		// lookup the forwarder message out

		ForwarderMessageOutRec forwarderMessageOut =
			forwarderMessageOutHelper.find (
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
					new ForwarderMessageOutReportRec ()

				.setForwarderMessageOut (
					forwarderMessageOut)

				.setIndex (
					forwarderMessageOut.getReportIndexNext ())

				.setOldMessageStatus (
					delivery.getOldMessageStatus ())

				.setNewMessageStatus (
					delivery.getNewMessageStatus ())

				.setCreatedTime (
					instantToDate (
						transaction.now ()))

			);

			// update the forwarder message out

			if (forwarderMessageOut.getReportIndexPending () == null) {

				forwarderMessageOut

					.setReportIndexPending (
						forwarderMessageOutReport.getIndex ())

					.setReportRetryTime (
						instantToDate (
							transaction.now ()))

					.setReportTries (
						0);

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
