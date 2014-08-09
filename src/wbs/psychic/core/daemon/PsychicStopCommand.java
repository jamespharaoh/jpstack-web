package wbs.psychic.core.daemon;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicRoutesRec;
import wbs.psychic.template.model.PsychicTemplateObjectHelper;
import wbs.psychic.template.model.PsychicTemplateRec;
import wbs.psychic.user.core.logic.PsychicUserLogic;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.outbox.logic.MessageSender;

@PrototypeComponent ("psychicStopCommand")
public
class PsychicStopCommand
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
	PsychicTemplateObjectHelper psychicTemplateHelper;

	@Inject
	PsychicUserLogic psychicUserLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"psychic.stop"
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
			commandHelper.find (commandId);

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

		// mark the use as stopped

		psychicUser.setStopped (true);

		// send a response

		sendStoppedMessage (
			psychicUser,
			message.getThreadId ());

		// create an event

		eventLogic.createEvent (
			"psychic_user_stopped",
			psychicUser,
			message);

		// mark message as processed

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

		// commit and return

		transaction.commit ();

		return null;

	}

	private MessageRec sendStoppedMessage (
			PsychicUserRec psychicUser,
			Integer threadId) {

		PsychicRec psychic =
			psychicUser.getPsychic ();

		PsychicRoutesRec routes =
			psychic.getRoutes ();

		ServiceRec service =
			serviceHelper.findByCode (psychic, "bill");

		PsychicAffiliateRec psychicAffiliate =
			psychicUser.getPsychicAffiliate ();

		AffiliateRec defaultAffiliate =
			affiliateHelper.findByCode(
				psychicAffiliate,
				"default");

		// send the message

		PsychicTemplateRec stopTemplate =
			psychicTemplateHelper.findByCode (
				psychic,
				"stop");

		MessageRec message =
			messageSender.get ()

			.threadId (
				threadId)

			.number (
				psychicUser.getNumber ())

			.messageText (
				stopTemplate.getTemplateText ())

			.numFrom (
				routes.getBillNumber ())

			.routerResolve (
				routes.getFreeRouter ())

			.service (
				service)

			.affiliate (
				defaultAffiliate)

			.send ();

		return message;

	}

}
