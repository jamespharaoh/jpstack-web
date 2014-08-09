package wbs.apn.chat.core.daemon;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.daemon.ReceivedMessageImpl;

@PrototypeComponent ("chatMagicCommand")
public
class ChatMagicCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatKeywordObjectHelper chatKeywordHelper;

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandManager commandManager;

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.magic"
		};

	}

	// implementation

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

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				command);

		String rest =
			receivedMessage.getRest ();

		// look for a single keyword

		for (KeywordFinder.Match match
				: KeywordFinder.find (rest)) {

			if (! rest.isEmpty ())
				continue;

			ChatKeywordRec chatKeyword =
				chatKeywordHelper.findByCode (
					chat,
					match.simpleKeyword ());

			if (chatKeyword != null
					&& chatKeyword.getGlobal ()
					&& chatKeyword.getCommand () != null) {

				transaction.commit ();

				ReceivedMessage newMessage =
					new ReceivedMessageImpl (
						receivedMessage,
						"");

				return commandManager.handle (
					chatKeyword.getCommand ().getId (),
					newMessage);

			}

		}

		transaction.commit ();

		// use the default command

		return commandManager.handle (
			receivedMessage.getRef (),
			receivedMessage);

	}

}