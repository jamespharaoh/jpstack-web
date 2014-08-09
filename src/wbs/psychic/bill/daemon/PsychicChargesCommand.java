package wbs.psychic.bill.daemon;

import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.user.core.logic.PsychicUserLogic;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.number.core.model.NumberRec;

@PrototypeComponent ("psychicChargesCommand")
public
class PsychicChargesCommand
	implements CommandHandler {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandObjectHelper commandHelper;

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
	RootObjectHelper rootHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"psychic.charges"
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

		NumberRec number =
			message.getNumber ();

		PsychicUserRec psychicUser =
			psychicUserLogic.findOrCreateUser (
				psychic,
				number);

		// search the message for the word "yes"

		String rest =
			receivedMessage.getRest ();

		boolean gotYes =
			yesPattern.matcher (rest).find ();

		// confirm charges if it was found

		if (gotYes)
			psychicUser.setChargesConfirmed (true);

		// perform the next step in the join process

		psychicUserLogic.join (
			psychicUser,
			message.getThreadId ());

		// mark the message as processed

		PsychicAffiliateRec psychicAffiliate =
			psychicUser.getPsychicAffiliate ();

		ServiceRec defaultService =
			serviceHelper.findByCode (psychic, "default");

		AffiliateRec affiliate =
			affiliateHelper.findByCode (
				psychicAffiliate,
				"default");

		inboxLogic.inboxProcessed (
			message,
			defaultService,
			affiliate,
			command);

		// and commit

		transaction.commit ();

		return null;

	}

	static Pattern yesPattern =
		Pattern.compile (
			"\\byes\\b",
			Pattern.CASE_INSENSITIVE);

}
