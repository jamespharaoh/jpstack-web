package wbs.smsapps.orderer.daemon;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.misc.MapStringSubstituter;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.smsapps.orderer.model.OrdererOrderObjectHelper;
import wbs.smsapps.orderer.model.OrdererOrderRec;
import wbs.smsapps.orderer.model.OrdererRec;
import wbs.utils.email.EmailLogic;

@SingletonComponent ("ordererDeliveryNoticeHandler")
public
class OrdererDeliveryNoticeHandler
	implements DeliveryHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@SingletonDependency
	EmailLogic emailLogic;

	@SingletonDependency
	OrdererOrderObjectHelper ordererOrderHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// details

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"orderer");

	}

	// implementation

	@Override
	public
	void handle (
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"OrdererDeliveryNoticeHandler.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		OrdererOrderRec order =
			ordererOrderHelper.findRequired (
				delivery.getMessage ().getRef ());

		OrdererRec orderer =
			order.getOrderer ();

		// if its not delivered or we've already seen delivery just
		// ignore it

		if (
			! delivery.getNewMessageStatus ().isGoodType ()
			|| order.getDeliveredTime () != null
		) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// update the order

		order

			.setDeliveredTime (
				transaction.now ());

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

		emailLogic.sendSystemEmail (
			ImmutableList.of (
				orderer.getEmailAddress ()),
			orderer.getEmailSubject (),
			emailBody);

		// remove the dnq

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
