package wbs.psychic.profile.daemon;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.affiliategroup.model.PsychicAffiliateGroupRec;
import wbs.psychic.bill.logic.PsychicBillLogic;
import wbs.psychic.contact.logic.PsychicContactLogic;
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.profile.model.PsychicProfileObjectHelper;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.request.model.PsychicRequestObjectHelper;
import wbs.psychic.request.model.PsychicRequestRec;
import wbs.psychic.user.core.logic.PsychicUserLogic;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

@PrototypeComponent ("psychicSendToProfileCommand")
public
class PsychicSendToProfileCommand
	implements CommandHandler {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	PsychicBillLogic psychicBillLogic;

	@Inject
	PsychicContactLogic psychicContactLogic;

	@Inject
	PsychicProfileObjectHelper psychicProfileHelper;

	@Inject
	PsychicRequestObjectHelper psychicRequestHelper;

	@Inject
	PsychicUserLogic psychicUserLogic;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"psychic.send_to_profile"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		PsychicRec psychic =
			(PsychicRec) (Object)
			objectManager.getParent (
				command);

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		PsychicUserRec psychicUser =
			psychicUserLogic.findOrCreateUser (
				psychic,
				message.getNumber ());

		PsychicProfileRec profile =
			psychicProfileHelper.find (
				receivedMessage.getRef ());

		PsychicContactRec contact =
			psychicContactLogic.findOrCreatePsychicContact (
				psychicUser,
				profile);

		PsychicAffiliateRec psychicAffiliate =
			psychicUser.getPsychicAffiliate ();

		AffiliateRec affiliate =
			affiliateHelper.findByCode (
				psychicAffiliate,
				"default");

		PsychicAffiliateGroupRec affiliateGroup =
			psychicAffiliate.getPsychicAffiliateGroup ();

		TextRec requestText =
			textHelper.findOrCreate (
				receivedMessage.getRest ());

		ServiceRec requestService =
			serviceHelper.findByCode (psychic, "request");

		// unstop user

		if (psychicUser.getStopped ()) {

			psychicUser.setStopped (false);

			eventLogic.createEvent (
				"psychic_user_unstopped",
				psychicUser,
				message);

		}

		// check credit

		boolean creditOk =
			psychicBillLogic.chargeOneRequest (
				psychicUser,
				message.getThreadId ());

		if (! creditOk) {

			inboxLogic.inboxProcessed (
				message,
				requestService,
				affiliate,
				command);

			transaction.commit ();

			return null;

		}

		// create request

		PsychicRequestRec request =
			psychicRequestHelper.insert (
				new PsychicRequestRec ()
					.setPsychicContact (contact)
					.setIndex (contact.getNumRequests ())
					.setPsychic (psychic)
					.setRequestTime (transaction.now ())
					.setRequestMessage (message)
					.setRequestText (requestText));

		// create queue item

		if (contact.getQueueItem () == null) {

			contact.setQueueItem (
				queueLogic.createQueueItem (
					queueLogic.findQueue (affiliateGroup, "request"),
					contact,
					request,
					objectManager.objectPath (
						psychicUser,
						psychic),
					requestText.getText ()));

		}

		// update contact

		if (contact.getFirstRequest () == null)
			contact.setFirstRequest (transaction.now ());

		contact.setNumRequests (contact.getNumRequests () + 1);

		contact.setLastRequest (transaction.now ());

		// mark inbox processed

		inboxLogic.inboxProcessed (
			message,
			requestService,
			affiliate,
			command);

		transaction.commit ();

		return null;

	}

}
