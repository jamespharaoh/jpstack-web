package wbs.smsapps.orderer.daemon;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.email.logic.EmailLogic;
import wbs.platform.misc.MapStringSubstituter;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.smsapps.orderer.model.OrdererOrderObjectHelper;
import wbs.smsapps.orderer.model.OrdererOrderRec;
import wbs.smsapps.orderer.model.OrdererRec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@SingletonComponent ("ordererDeliveryNoticeHandler")
public
class OrdererDeliveryNoticeHandler
	implements DeliveryHandler {

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	EmailLogic emailLogic;

	@Inject
	OrdererOrderObjectHelper ordererOrderHelper;

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"orderer");

	}

	@Override
	public
	void handle (
			int deliveryId,
			Integer ref) {

		Date now =
			new Date ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		OrdererOrderRec order =
			ordererOrderHelper.find (
				delivery.getMessage ().getRef ());

		OrdererRec orderer =
			order.getOrderer ();

		// if its not delivered or we've already seen delivery just
		// ignore it

		if (! delivery.getNewMessageStatus ().isGoodType ()
				|| order.getDeliveredTime () != null) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// update the order

		order
			.setDeliveredTime (now);

		// construct the message

		String emailBody =
			MapStringSubstituter.substitute (
				order.getOrderer ().getEmailTemplate (),
				ImmutableMap.<String,String>builder ()

					.put ("message",
						order.getReceivedMessage ()
							.getText ()
							.getText ())

					.put ("number",
						order.getNumber ()
							.getNumber ())

					.build ());

		// send the email

		emailLogic.sendEmail (
			orderer.getEmailAddress (),
			orderer.getEmailSubject (),
			emailBody);

		// remove the dnq

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
