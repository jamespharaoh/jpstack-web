package wbs.apn.chat.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.logic.CommandLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

import com.google.common.base.Optional;

/**
 * MainCommandHandler takes input from the main chat interface, looking for
 * keywords or box numbers and forwarding to the appropriate command.
 */
@Log4j
@PrototypeComponent ("chatMainCommand")
public
class ChatMainCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatKeywordObjectHelper chatKeywordHelper;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatSchemeKeywordObjectHelper chatSchemeKeywordHelper;

	@Inject
	ChatSchemeObjectHelper chatSchemeHelper;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandLogic commandLogic;

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
	Provider<ChatJoiner> chatJoinerProvider;

	// state

	CommandRec mainCommand;
	ChatSchemeRec commandChatScheme;
	ChatRec chat;
	MessageRec smsMessage;
	ChatUserRec fromChatUser;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat_scheme.chat_scheme"
		};

	}

	// implementation

	void doCode (
			int commandId,
			@NonNull ReceivedMessage receivedMessage,
			String code,
			String rest) {

		ChatUserRec toUser =
			chatUserHelper.findByCode (
				chat,
				code);

		ChatSchemeRec userChatScheme =
			fromChatUser.getChatScheme ();

		if (toUser == null) {

			log.debug (
				stringFormat (
					"message %d: ignoring invalid user code %s",
					receivedMessage.getMessageId (),
					code));

			inboxLogic.inboxProcessed (
				smsMessage,
				serviceHelper.findByCode (chat, "default"),
				chatUserLogic.getAffiliate (fromChatUser),
				commandHelper.find (commandId));

			return;
		}

		log.debug (
			stringFormat (
				"message %d: message to user %s",
				receivedMessage.getMessageId (),
				toUser.getId ()));

		chatMessageLogic.chatMessageSendFromUser (
			fromChatUser,
			toUser,
			rest,
			smsMessage.getThreadId (),
			ChatMessageMethod.sms,
			null);

		// send signup info if relevant

		if (fromChatUser.getFirstJoin () == null) {

			chatSendLogic.sendSystem (
				fromChatUser,
				Optional.of (
					smsMessage.getThreadId ()),
				"message_signup",
				userChatScheme.getRbFreeRouter (),
				userChatScheme.getRbNumber (),
				Collections.<String>emptySet (),
				Optional.<String>absent (),
				"system",
				Collections.<String,String>emptyMap ());

			chatSendLogic.sendSystemMagic (
				fromChatUser,
				Optional.of (
					smsMessage.getThreadId ()),
				"dob_request",
				commandHelper.findByCode (
					chat,
					"magic"),
				commandHelper.findByCode (
					userChatScheme,
					"chat_dob"
				).getId (),
				Collections.<String,String>emptyMap ());

		}

		inboxLogic.inboxProcessed (
			smsMessage,
			serviceHelper.findByCode (
				chat,
				"default"),
			chatUserLogic.getAffiliate (
				fromChatUser),
			commandHelper.find (
				commandId));

	}

	/**
	 * Tries to find a ChatSchemeKeyword to handle this message. Returns an
	 * appropriate CommandHandler if so, otherwise returns null.
	 */
	TryKeywordReturn trySchemeKeyword (
			int commandId,
			ReceivedMessage receivedMessage,
			String keyword,
			String rest) {

		TryKeywordReturn ret =
			new TryKeywordReturn ();

		ChatSchemeKeywordRec chatSchemeKeyword =
			chatSchemeKeywordHelper.findByCode (
				commandChatScheme,
				keyword);

		if (chatSchemeKeyword == null) {

			log.debug (
				stringFormat (
					"message %d: no chat scheme keyword \"%s\"",
					receivedMessage.getMessageId (),
					keyword));

			return null;

		}

		if (chatSchemeKeyword.getJoinType () != null) {

			log.debug (
				stringFormat (
					"message %d: chat scheme keyword \"%s\" is join type %s",
					receivedMessage.getMessageId (),
					keyword,
					chatSchemeKeyword.getJoinType ()));

			Integer chatAffiliateId =
				chatSchemeKeyword.getJoinChatAffiliate () != null
					? chatSchemeKeyword.getJoinChatAffiliate ().getId ()
					: null;

			ret.joiner =
				chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					ChatJoiner.convertJoinType (
						chatSchemeKeyword.getJoinType ()))

				.gender (
					chatSchemeKeyword.getJoinGender ())

				.orient (
					chatSchemeKeyword.getJoinOrient ())

				.chatAffiliateId (
					chatAffiliateId)

				.chatSchemeId (
					commandChatScheme.getId ())

				.confirmCharges (
					chatSchemeKeyword.getConfirmCharges ());

			ret.rest =
				rest;

			ret.creditCheck =
				true;

			return ret;

		}

		if (chatSchemeKeyword.getCommand () != null) {

			log.debug (
				stringFormat (
					"message %d: chat scheme keyword \"%s\" is command %s",
					receivedMessage.getMessageId (),
					keyword,
					chatSchemeKeyword.getCommand ().getId ()));

			ret.externalCommandId =
				chatSchemeKeyword.getCommand ().getId ();

			ret.rest = rest;

			ret.creditCheck = true;

			return ret;

		}

		// this keyword does nothing?

		log.warn (
			stringFormat (
				"message %d: chat scheme keyword \"%s\" does nothing",
				receivedMessage.getMessageId (),
				keyword,
				chatSchemeKeyword.getJoinType ()));

		return null;

	}

	TryKeywordReturn tryChatKeyword (
			int commandId,
			ReceivedMessage receivedMessage,
			String keyword,
			String rest) {

		TryKeywordReturn ret = new TryKeywordReturn ();

		ChatKeywordRec chatKeyword =
			chatKeywordHelper.findByCode (
				chat,
				keyword);

		if (chatKeyword == null) {

			log.debug (
				stringFormat (
					"message %d: no chat keyword \"%s\"",
					receivedMessage.getMessageId (),
					keyword));

			return null;

		}

		if (chatKeyword.getJoinType () != null) {

			log.debug (
				stringFormat (
					"message %d: chat keyword \"%s\" is join type %s",
					receivedMessage.getMessageId (),
					keyword,
					chatKeyword.getJoinType ()));

			Integer chatAffiliateId =
				chatKeyword.getJoinChatAffiliate () != null
					? chatKeyword.getJoinChatAffiliate ().getId ()
					: null;

			ret.joiner =
				chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					ChatJoiner.convertJoinType (
						chatKeyword.getJoinType ()))

				.gender (
					chatKeyword.getJoinGender ())

				.orient (
					chatKeyword.getJoinOrient ())

				.chatAffiliateId (
					chatAffiliateId)

				.chatSchemeId (
					commandChatScheme.getId ());

			ret.rest =
				rest;

			ret.creditCheck =
				! chatKeyword.getNoCreditCheck ();

			return ret;

		}

		if (chatKeyword.getCommand () != null) {

			log.debug (
				stringFormat (
					"message %d: chat keyword \"%s\" is command %d",
					receivedMessage.getMessageId (),
					keyword,
					chatKeyword.getCommand ().getId ()));

			ret.externalCommandId =
				chatKeyword.getCommand ().getId ();

			ret.rest =
				rest;

			ret.creditCheck =
				false;

			return ret;

		}

		// this keyword does nothing

		log.warn (
			stringFormat (
				"message %d: chat keyword \"%s\" does nothing",
				receivedMessage.getMessageId (),
				keyword));

		return null;

	}

	TryKeywordReturn tryKeyword (
			int commandId,
			ReceivedMessage receivedMessage,
			String keyword,
			String rest) {

		TryKeywordReturn returnValue;

		returnValue =
			trySchemeKeyword (
				commandId,
				receivedMessage,
				keyword,
				rest);

		if (returnValue != null)
			return returnValue;

		returnValue =
			tryChatKeyword (
				commandId,
				receivedMessage,
				keyword,
				rest);

		if (returnValue != null)
			return returnValue;

		return null;

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		log.debug (
			stringFormat (
				"message %d: begin processing",
				receivedMessage.getMessageId ()));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		mainCommand =
			commandHelper.find (
				commandId);

		commandChatScheme =
			chatSchemeHelper.find (
				mainCommand.getParentObjectId ());

		chat =
			commandChatScheme.getChat ();

		smsMessage =
			messageHelper.find (
				receivedMessage.getMessageId ());

		fromChatUser =
			chatUserHelper.findOrCreate (
				chat,
				smsMessage);

		log.debug (
			stringFormat (
				"message %d: full text \"%s\"",
				receivedMessage.getMessageId (),
				smsMessage.getText ().getText ()));

		log.debug (
			stringFormat (
				"message %d: rest \"%s\"",
				receivedMessage.getMessageId (),
				receivedMessage.getRest ()));

		// set chat scheme and adult verify

		chatUserLogic.setScheme (
			fromChatUser,
			commandChatScheme);

		if (smsMessage.getRoute ().getInboundImpliesAdult ())
			chatUserLogic.adultVerify (fromChatUser);

		// look for a keyword

		TryKeywordReturn ret = null;

		for (KeywordFinder.Match match
				: KeywordFinder.find (
					receivedMessage.getRest ())) {

			String keyword =
				match.simpleKeyword ();

			log.debug (
				stringFormat (
					"message %d: trying keyword \"%s\"",
					receivedMessage.getMessageId (),
					keyword));

			// check if the keyword is a 6-digit number

			if (keyword.matches ("\\d{6}")) {

				doCode (
					commandId,
					receivedMessage,
					keyword,
					match.rest ());

				transaction.commit ();

				return null;

			}

			// check if it's a chat keyword

			ret =
				tryKeyword (
					commandId,
					receivedMessage,
					keyword,
					match.rest ());

			if (ret != null)
				break;

		}

		// handle command keywords

		if (ret != null && ret.externalCommandId != null) {

			log.debug (
				stringFormat (
					"message %d: external keyword found, handing off",
					receivedMessage.getMessageId ()));

			transaction.commit ();

			return commandManager.handle (
				ret.externalCommandId,
				receivedMessage,
				ret.rest);

		}

		// send barred users to help

		boolean performCreditCheck =
			ret != null
				? ret.creditCheck
				: true;

		if (performCreditCheck) {

			log.debug (
				stringFormat (
					"message %d: performing credit check",
					receivedMessage.getMessageId ()));

			if (fromChatUser.getNumber ().getNetwork ().getId () == 0) {

				log.debug (
					stringFormat (
						"message %d: network unknown, ignoring",
						receivedMessage.getMessageId ()));

				inboxLogic.inboxNotProcessed (
					smsMessage,
					serviceHelper.findByCode (chat, "default"),
					chatUserLogic.getAffiliate (fromChatUser),
					mainCommand,
					stringFormat (
						"network unknown"));

				transaction.commit ();

				return null;

			}

			if (! chatCreditLogic.userSpendCheck (
				fromChatUser,
				true,
				smsMessage.getThreadId (),
				false)) {

				log.debug (
					stringFormat (
						"message %d: credit check failed, sending to help",
						receivedMessage.getMessageId ()));

				chatHelpLogLogic.createChatHelpLogIn (
					fromChatUser,
					smsMessage,
					receivedMessage.getRest (),
					null,
					true);

				inboxLogic.inboxProcessed (
					smsMessage,
					serviceHelper.findByCode (chat, "default"),
					chatUserLogic.getAffiliate (fromChatUser),
					commandHelper.find (commandId));

				transaction.commit ();

				return null;

			}

			log.debug (
				stringFormat (
					"message %d: not performing credit check",
					receivedMessage.getMessageId ()));

		}

		// handle keywords

		if (ret != null) {

			log.debug (
				stringFormat (
					"message %d: chat keyword found, handing off",
					receivedMessage.getMessageId ()));

			transaction.commit ();

			if (ret.externalCommandId != null) {

				return commandManager.handle (
					ret.externalCommandId,
					receivedMessage,
					ret.rest);

			} else {

				return ret.joiner.handle (
					receivedMessage,
					ret.rest);

			}

		}

		// no keyword found

		if (fromChatUser.getLastJoin () == null) {

			log.debug (
				stringFormat (
					"message %d: no keyword found, existing user, joining",
					receivedMessage.getMessageId ()));

			ChatJoiner joiner =
				chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					JoinType.chatSimple)

				.chatSchemeId (
					commandChatScheme.getId ());

			transaction.commit ();

			return joiner.handle (
				receivedMessage);

		} else {

			log.debug (
				stringFormat (
					"message %d: ",
					receivedMessage.getMessageId (),
					"no keyword found, existing user, sent to help"));

			chatHelpLogLogic.createChatHelpLogIn (
				fromChatUser,
				smsMessage,
				receivedMessage.getRest (),
				null,
				true);

			inboxLogic.inboxProcessed (
				smsMessage,
				serviceHelper.findByCode (
					chat,
					"default"),
				chatUserLogic.getAffiliate (
					fromChatUser),
				commandHelper.find (
					commandId));

			transaction.commit ();

			return null;

		}

	}

	// data structures

	public static
	class TryKeywordReturn {

		ChatJoiner joiner;
		Integer externalCommandId;
		String rest;
		boolean creditCheck;

	}

}
