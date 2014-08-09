package wbs.psychic.help.daemon;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.help.model.PsychicHelpRequestRec;
import wbs.psychic.user.core.logic.PsychicUserLogic;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

@PrototypeComponent ("psychicHelpCommandHandler")
public
class PsychicHelpCommandHandler
	implements CommandHandler {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	PsychicUserLogic psychicUserLogic;

	@Inject
	QueueLogic queueLogic;

	@Inject
	TextObjectHelper textHelper;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"psychic.help"
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

		// create the help request

		TextRec requestText =
			textHelper.findOrCreate (
				receivedMessage.getRest ());

		PsychicHelpRequestRec psychicHelpRequest =
			objectManager.insert (
				new PsychicHelpRequestRec ()
					.setPsychicUser (psychicUser)
					.setIndex (psychicUser.getNumHelpRequests ())
					.setRequestMessage (message)
					.setRequestText (requestText)
					.setRequestTime (transaction.now ()));

		// update the user

		psychicUser.setNumHelpRequests (
			psychicUser.getNumHelpRequests () + 1);

		// create a queue item if needed

		if (psychicUser.getHelpQueueItem () == null) {

			psychicUser.setHelpQueueItem (
				queueLogic.createQueueItem (
					queueLogic.findQueue (psychic, "help"),
					psychicUser,
					psychicHelpRequest,
					objectManager.objectPath (
						psychicUser,
						psychic),
					requestText.getText ()));

		}

		// mark the message as processed

		ServiceRec service =
			serviceHelper.findByCode (psychic, "help");

		PsychicAffiliateRec psychicAffiliate =
			psychicUser.getPsychicAffiliate ();

		AffiliateRec affiliate =
			affiliateHelper.findByCode (
				psychicAffiliate,
				"default");

		inboxLogic.inboxProcessed (
			message,
			service,
			affiliate,
			command);

		// and we are done

		transaction.commit ();

		return null;

	}

}
