package wbs.apn.chat.contact.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatBlockObjectHelper;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.gsm.Gsm;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.daemon.ReceivedMessageImpl;
import wbs.sms.message.inbox.logic.InboxLogic;

import com.google.common.base.Optional;

@Log4j
@PrototypeComponent ("chatChatCommand")
public
class ChatChatCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatBlockObjectHelper chatBlockHelper;

	@Inject
	ChatKeywordObjectHelper chatKeywordHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandManager commandManager;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	Database database;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	Provider<ChatJoiner> joiner;

	// state

	Status status;

	CommandRec thisCommand;
	ChatRec chat;
	MessageRec message;
	ChatUserRec fromChatUser;
	ChatUserRec toChatUser;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.chat"
		};

	}

	// implementation

	private
	boolean doBlock (
			Transaction transaction,
			ReceivedMessage receivedMessage) {

		// process inbox

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		inboxLogic.inboxProcessed (
			message,
			defaultService,
			chatUserLogic.getAffiliate (fromChatUser),
			thisCommand);

		// create the chatblock, if it doesn't already exist

		ChatBlockRec chatBlock =
			chatBlockHelper.find (
				fromChatUser,
				toChatUser);

		if (chatBlock == null) {

			chatBlockHelper.insert (
				new ChatBlockRec ()
					.setChatUser (fromChatUser)
					.setBlockedChatUser (toChatUser));

		}

		// send message through magic number

		TextRec text =
			textHelper.findOrCreate (
				stringFormat (
					"User %s has now been blocked",
					toChatUser.getCode ()));

		CommandRec magicCommand =
			commandHelper.findByCode (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCode (
				chat,
				"help");

		ServiceRec systemService =
			serviceHelper.findByCode (
				chat,
				"system");

		chatSendLogic.sendMessageMagic (
			fromChatUser,
			Optional.of (message.getThreadId ()),
			text,
			magicCommand,
			systemService,
			helpCommand.getId ());

		transaction.commit ();

		return true;

	}

	private
	boolean doInfo () {

		// TODO why is there no code here?

		throw new RuntimeException ("TODO");

	}

	private
	boolean doChat (
			Transaction transaction,
			ReceivedMessage receivedMessage) {

		if (toChatUser == null) {

			log.warn (
				stringFormat (
					"Message %d ignored as recipient user id %d does not exist",
					message.getId (),
					receivedMessage.getRef ()));

			ServiceRec defaultService =
				serviceHelper.findByCode (
					chat,
					"default");

			inboxLogic.inboxProcessed (
				message,
				defaultService,
				chatUserLogic.getAffiliate (fromChatUser),
				thisCommand);

			transaction.commit ();

			return true;

		}

		// process inbox

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		inboxLogic.inboxProcessed (
			message,
			defaultService,
			chatUserLogic.getAffiliate (fromChatUser),
			thisCommand);

		String rejected =
			chatMessageLogic.chatMessageSendFromUser (
				fromChatUser,
				toChatUser,
				receivedMessage.getRest (),
				message.getThreadId (),
				ChatMessageMethod.sms,
				null);

		if (rejected != null)
			message.setNotes (rejected);

		// do auto join

		if (chat.getAutoJoinOnSend ()) {

			chatMiscLogic.userAutoJoin (
				fromChatUser,
				message);

		}

		transaction.commit ();

		return true;

	}

	private
	boolean checkKeyword (
			Transaction transaction,
			ReceivedMessage receivedMessage,
			String keyword,
			String rest) {

		ChatKeywordRec chatKeyword =
			chatKeywordHelper.findByCode (
				chat,
				Gsm.toSimpleAlpha (keyword));

		if (chatKeyword == null)
			return false;

		if (chatKeyword.getChatBlock ()) {

			return doBlock (
				transaction,
				receivedMessage);

		}

		if (chatKeyword.getChatInfo ())
			return doInfo ();

		if (chatKeyword.getGlobal ()
			&& chatKeyword.getCommand () != null) {

			transaction.commit ();

			ReceivedMessage newMessage =
				new ReceivedMessageImpl (
					receivedMessage,
					receivedMessage.getMessageId (),
					rest,
					0);

			status =
				commandManager.handle (
					chatKeyword.getCommand ().getId (),
					newMessage);


			return true;

		}

		return false;

	}

	private
	boolean tryJoin (
			Transaction transaction,
			ReceivedMessage receivedMessage) {

		if (chatUserLogic.getAffiliateId (fromChatUser) != null)
			return false;

		// TODO the scheme is set randomly here

		// TODO how does this code even get invoked? if a user has not
		// joined then how can they be replying to a magic number from a
		// user. maybe from a broadcast, but that is all a bit fucked up.

		log.warn (
			stringFormat (
				"Chat request from unjoined user %d",
				fromChatUser.getId ()));

		ChatSchemeRec chatScheme =
			chat.getChatSchemes ().iterator ().next ();

		ChatJoiner joiner =
			this.joiner.get ()
				.chatId (chat.getId ())
				.joinType (JoinType.chatSimple)
				.chatSchemeId (chatScheme.getId ());

		transaction.commit ();

		status =
			joiner.handle (receivedMessage);

		return true;

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		thisCommand =
			commandHelper.find (
				commandId);

		chat =
			chatHelper.find (
				thisCommand.getParentObjectId ());

		message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		fromChatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		toChatUser =
			chatUserHelper.find (
				receivedMessage.getRef ());

		// treat as join if the user has no affiliate

		if (tryJoin (
				transaction,
				receivedMessage))
			return status;

		receivedMessage.setAffiliateId (
			chatUserLogic.getAffiliateId (fromChatUser));

		String rest =
			receivedMessage.getRest ();

		// look for keywords to interpret

		for (KeywordFinder.Match match
				: KeywordFinder.find (rest)) {

			if (! rest.isEmpty ())
				continue;

			if (checkKeyword (
				transaction,
				receivedMessage,
				match.simpleKeyword (),
				""))

				return status;

		}

		// send the message to the other user

		if (
			doChat (
				transaction,
				receivedMessage)
		) {
			return status;
		}

		throw new RuntimeException ();

	}

}