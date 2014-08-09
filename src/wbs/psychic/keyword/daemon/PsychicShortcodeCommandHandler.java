package wbs.psychic.keyword.daemon;

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
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicSettingsRec;
import wbs.psychic.keyword.model.PsychicKeywordRec;
import wbs.psychic.keyword.model.PsychicKeywordType;
import wbs.psychic.user.core.logic.PsychicUserLogic;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.number.core.model.NumberRec;

@PrototypeComponent ("psychicShortcodeCommandHandler")
public
class PsychicShortcodeCommandHandler
	implements CommandHandler {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandManager commandManager;

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
	PsychicUserLogic psychicUserLogic;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"psychic.shortcode"
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

		RootRec root =
			rootHelper.find (0);

		CommandRec command =
			commandHelper.find (
				commandId);

		PsychicRec psychic =
			(PsychicRec) (Object)
			objectManager.getParent (
				command);

		PsychicSettingsRec settings =
			psychic.getSettings ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		NumberRec number =
			message.getNumber ();

		PsychicUserRec psychicUser =
			psychicUserLogic.findOrCreateUser (
				psychic,
				number);

		// try and find a keyword

		for (KeywordFinder.Match match
				: KeywordFinder.find (receivedMessage.getRest ())) {

			PsychicKeywordRec keyword =
				objectManager.findChildByCode (
					PsychicKeywordRec.class,
					psychic,
					match.simpleKeyword ());

			if (keyword == null)
				continue;

			if (keyword.getType () == null)
				throw new RuntimeException ();

			// command keyword

			if (keyword.getType () == PsychicKeywordType.command) {

				return commandManager.handle (
					keyword.getTargetCommand ().getId (),
					receivedMessage,
					match.rest ());

			}

			// set affiliate

			if (psychicUser.getPsychicAffiliate () == null
				|| psychicUser.getPsychicAffiliate ()
					== settings.getDefaultPsychicAffiliate ()) {

				psychicUser.setPsychicAffiliate (
					keyword.getJoinAffiliate ());

			}

			// unstop the user

			if (psychicUser.getStopped ()) {

				psychicUser.setStopped (false);

				eventLogic.createEvent (
					"psychic_user_unstopped",
					psychicUser,
					message);

			}

			// join the user

			psychicUserLogic.join (
				psychicUser,
				message.getThreadId ());

			// and complete

			ServiceRec defaultService =
				serviceHelper.findByCode (
					psychic,
					"default");

			AffiliateRec systemAffiliate =
				affiliateHelper.findByCode (
					root,
					"system");

			inboxLogic.inboxProcessed (
				message,
				defaultService,
				systemAffiliate,
				command);

			transaction.commit ();

			return null;

		}

		// join without keyword

		if (psychicUser.getPsychicAffiliate () != null) {

			// join the user

			psychicUserLogic.join (
				psychicUser,
				message.getThreadId ());

			// and complete

			ServiceRec defaultService =
				serviceHelper.findByCode (
					psychic,
					"default");

			AffiliateRec systemAffiliate =
				affiliateHelper.findByCode (
					root,
					"system");

			inboxLogic.inboxProcessed (
				message,
				defaultService,
				systemAffiliate,
				command);

			transaction.commit ();

			return null;

		}

		// not processed

		ServiceRec defaultService =
			serviceHelper.findByCode (
				psychic,
				"default");

		AffiliateRec systemAffiliate =
			affiliateHelper.findByCode (
				root,
				"system");

		inboxLogic.inboxNotProcessed (
			message,
			defaultService,
			systemAffiliate,
			command,
			"No keyword or default affiliate");

		transaction.commit ();

		return null;

	}

}
