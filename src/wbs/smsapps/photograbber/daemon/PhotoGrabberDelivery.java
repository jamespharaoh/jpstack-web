package wbs.smsapps.photograbber.daemon;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.photograbber.model.PhotoGrabberRec;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestObjectHelper;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestRec;

@SingletonComponent ("photoGrabberDelivery")
public
class PhotoGrabberDelivery
	implements DeliveryHandler {

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	PhotoGrabberRequestObjectHelper photoGrabberRequestHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"photo_grabber");

	}

	@Override
	public
	void handle (
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"PhotoGrabberDelivery.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		PhotoGrabberRequestRec photoGrabberRequest =
			photoGrabberRequestHelper.findByBilledMessage (
				message);

		if (
			delivery.getNewMessageStatus () != MessageStatus.delivered
			|| photoGrabberRequest.getResponseTime () != null
		) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		photoGrabberRequest

			.setResponseTime (
				transaction.now ());

		PhotoGrabberRec photoGrabber =
			photoGrabberRequest.getPhotoGrabber ();

		messageSender.get ()

			.threadId (
				message.getThreadId ())

			.number (
				message.getNumber ())

			.messageString (
				photoGrabber.getMmsTemplate ())

			.numFrom (
				photoGrabber.getMmsNumber ())

			.route (
				photoGrabber.getMmsRoute ())

			.serviceLookup (
				photoGrabber,
				"default")

			.subjectString (
				photoGrabber.getMmsSubject ())

			.medias (
				Collections.singletonList (
					photoGrabberRequest.getMedia ()))

			.send ();

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
