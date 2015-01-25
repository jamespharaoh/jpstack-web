package wbs.apn.chat.core.daemon;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.joda.time.LocalDate;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
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
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.logic.CommandLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.DateFinder;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

/**
 * MainCommandHandler takes input from the main chat interface, looking for
 * keywords or box numbers and forwarding to the appropriate command.
 */
@Accessors (fluent = true)
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
	KeywordFinder keywordFinder;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Provider<ChatJoiner> chatJoinerProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// state

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

	InboxAttemptRec doCode (
			@NonNull String code,
			@NonNull String rest) {

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
					inbox.getId (),
					code));

			return inboxLogic.inboxProcessed (
				smsMessage,
				Optional.of (
					serviceHelper.findByCode (
						chat,
						"default")),
				Optional.of (
					chatUserLogic.getAffiliate (
						fromChatUser)),
				command);

		}

		log.debug (
			stringFormat (
				"message %d: message to user %s",
				inbox.getId (),
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

		return inboxLogic.inboxProcessed (
			smsMessage,
			Optional.of (
				serviceHelper.findByCode (
					chat,
					"default")),
			Optional.of (
				chatUserLogic.getAffiliate (
					fromChatUser)),
			command);

	}

	/**
	 * Tries to find a ChatSchemeKeyword to handle this message. Returns an
	 * appropriate CommandHandler if so, otherwise returns null.
	 */
	Optional<InboxAttemptRec> trySchemeKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		ChatSchemeKeywordRec chatSchemeKeyword =
			chatSchemeKeywordHelper.findByCode (
				commandChatScheme,
				keyword);

		if (chatSchemeKeyword == null) {

			log.debug (
				stringFormat (
					"message %d: no chat scheme keyword \"%s\"",
					inbox.getId (),
					keyword));

			return Optional.<InboxAttemptRec>absent ();

		}

		if (chatSchemeKeyword.getJoinType () != null) {

			log.debug (
				stringFormat (
					"message %d: chat scheme keyword \"%s\" is join type %s",
					inbox.getId (),
					keyword,
					chatSchemeKeyword.getJoinType ()));

			Optional<InboxAttemptRec> inboxAttempt =
				performCreditCheck ();

			if (inboxAttempt.isPresent ())
				return inboxAttempt;

			Integer chatAffiliateId =
				chatSchemeKeyword.getJoinChatAffiliate () != null
					? chatSchemeKeyword.getJoinChatAffiliate ().getId ()
					: null;

			return Optional.of (
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
					chatSchemeKeyword.getConfirmCharges ())

				.rest (
					rest)

				.handleInbox (
					command)

			);

		}

		if (chatSchemeKeyword.getCommand () != null) {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"chat scheme keyword \"%s\" ",
					keyword,
					"is command %s",
					chatSchemeKeyword.getCommand ().getId ()));

			Optional<InboxAttemptRec> inboxAttempt =
				performCreditCheck ();

			if (inboxAttempt.isPresent ())
				return inboxAttempt;

			return Optional.of (
				commandManager.handle (
					inbox,
					chatSchemeKeyword.getCommand (),
					Optional.<Integer>absent (),
					rest));

		}

		// this keyword does nothing?

		log.warn (
			stringFormat (
				"message %d: chat scheme keyword \"%s\" does nothing",
				inbox.getId (),
				keyword));

		return Optional.<InboxAttemptRec>absent ();

	}

	Optional<InboxAttemptRec> tryChatKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		ChatKeywordRec chatKeyword =
			chatKeywordHelper.findByCode (
				chat,
				keyword);

		if (chatKeyword == null) {

			log.debug (
				stringFormat (
					"message %d: no chat keyword \"%s\"",
					inbox.getId (),
					keyword));

			return Optional.<InboxAttemptRec>absent ();

		}

		if (chatKeyword.getJoinType () != null) {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"chat keyword \"%s\" ",
					keyword,
					"is join type %s",
					chatKeyword.getJoinType ()));

			Integer chatAffiliateId =
				chatKeyword.getJoinChatAffiliate () != null
					? chatKeyword.getJoinChatAffiliate ().getId ()
					: null;

			if (! chatKeyword.getNoCreditCheck ()) {

				Optional<InboxAttemptRec> inboxAttempt =
					performCreditCheck ();

				if (inboxAttempt.isPresent ())
					return inboxAttempt;

			}

			return Optional.of (
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
					commandChatScheme.getId ())

				.rest (
					rest)

				.handleInbox (
					command)

			);

		}

		if (chatKeyword.getCommand () != null) {

			log.debug (
				stringFormat (
					"message %d: chat keyword \"%s\" is command %d",
					inbox.getId (),
					keyword,
					chatKeyword.getCommand ().getId ()));

			return Optional.of (
				commandManager.handle (
					inbox,
					chatKeyword.getCommand (),
					Optional.<Integer>absent (),
					rest));

		}

		// this keyword does nothing

		log.warn (
			stringFormat (
				"message %d: ",
				inbox.getId (),
				"chat keyword \"%s\" ",
				keyword,
				"does nothing"));

		return Optional.<InboxAttemptRec>absent ();

	}

	Optional<InboxAttemptRec> tryKeyword (
			@NonNull String keyword,
			@NonNull String rest) {

		Optional<InboxAttemptRec> schemeKeywordInboxAttempt =
			trySchemeKeyword (
				keyword,
				rest);

		if (schemeKeywordInboxAttempt.isPresent ())
			return schemeKeywordInboxAttempt;

		Optional<InboxAttemptRec> chatKeywordInboxAttempt =
			tryChatKeyword (
				keyword,
				rest);

		if (chatKeywordInboxAttempt.isPresent ())
			return chatKeywordInboxAttempt;

		return Optional.<InboxAttemptRec>absent ();

	}

	Optional<InboxAttemptRec> tryDob () {

		if (fromChatUser.getFirstJoin () != null)
			return Optional.<InboxAttemptRec>absent ();

		if (
			! in (
				fromChatUser.getNextJoinType (),
				ChatKeywordJoinType.chatDob,
				ChatKeywordJoinType.dateDob)
		) {
			return Optional.<InboxAttemptRec>absent ();
		}

		LocalDate dateOfBirth =
			DateFinder.find (
				rest,
				1915);

		if (dateOfBirth == null)
			return Optional.<InboxAttemptRec>absent ();

		return Optional.of (
			chatJoinerProvider.get ()

			.chatId (
				chat.getId ())

			.joinType (
				JoinType.chatDob)

			.chatSchemeId (
				commandChatScheme.getId ())

			.handleInbox (
				command)

		);

	}

	@Override
	public
	InboxAttemptRec handle () {

		log.debug (
			stringFormat (
				"message %d: begin processing",
				inbox.getId ()));

		commandChatScheme =
			chatSchemeHelper.find (
				command.getParentObjectId ());

		chat =
			commandChatScheme.getChat ();

		smsMessage =
			inbox.getMessage ();

		fromChatUser =
			chatUserHelper.findOrCreate (
				chat,
				smsMessage);

		log.debug (
			stringFormat (
				"message %d: full text \"%s\"",
				inbox.getId (),
				smsMessage.getText ().getText ()));

		log.debug (
			stringFormat (
				"message %d: rest \"%s\"",
				inbox.getId (),
				rest));

		// set chat scheme and adult verify

		chatUserLogic.setScheme (
			fromChatUser,
			commandChatScheme);

		if (smsMessage.getRoute ().getInboundImpliesAdult ()) {

			chatUserLogic.adultVerify (
				fromChatUser);

		}

		// look for a date of birth

		Optional<InboxAttemptRec> dobInboxAttempt =
			tryDob ();

		if (dobInboxAttempt.isPresent ())
			return dobInboxAttempt.get ();

		// look for a keyword

		for (
			KeywordFinder.Match match
				: keywordFinder.find (
					rest)
		) {

			log.debug (
				stringFormat (
					"message %d: trying keyword \"%s\"",
					inbox.getId (),
					match.simpleKeyword ()));

			// check if the keyword is a 6-digit number

			if (match.simpleKeyword ().matches ("\\d{6}")) {

				return doCode (
					match.simpleKeyword (),
					match.rest ());

			}

			// check if it's a chat keyword

			Optional<InboxAttemptRec> keywordInboxAttempt =
				tryKeyword (
					match.simpleKeyword (),
					match.rest ());

			if (keywordInboxAttempt.isPresent ())
				return keywordInboxAttempt.get ();

		}

		// no keyword found

		if (fromChatUser.getLastJoin () == null) {

			if (chat.getErrorOnUnrecognised ()) {

				log.debug (
					stringFormat (
						"message %d: ",
						inbox.getId (),
						"no keyword found, new user, sending error"));

				chatHelpLogLogic.createChatHelpLogIn (
					fromChatUser,
					smsMessage,
					rest,
					null,
					false);

				chatSendLogic.sendSystemRbFree (
					fromChatUser,
					Optional.of (
						smsMessage.getThreadId ()),
					"keyword_error",
					Collections.<String,String>emptyMap ());

				return inboxLogic.inboxProcessed (
					smsMessage,
					Optional.of (
						serviceHelper.findByCode (
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							fromChatUser)),
					command);

			} else {

				log.debug (
					stringFormat (
						"message %d: ",
						inbox.getId (),
						"no keyword found, new user, joining"));

				return chatJoinerProvider.get ()

					.chatId (
						chat.getId ())

					.joinType (
						JoinType.chatSimple)

					.chatSchemeId (
						commandChatScheme.getId ())

					.rest (
						rest)

					.handleInbox (
						command);

			}

		} else {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"no keyword found, existing user, sent to help"));

			chatHelpLogLogic.createChatHelpLogIn (
				fromChatUser,
				smsMessage,
				rest,
				null,
				true);

			return inboxLogic.inboxProcessed (
				smsMessage,
				Optional.of (
					serviceHelper.findByCode (
						chat,
						"default")),
				Optional.of (
					chatUserLogic.getAffiliate (
						fromChatUser)),
				command);

		}

	}

	Optional<InboxAttemptRec> performCreditCheck () {

		log.debug (
			stringFormat (
				"message %d: performing credit check",
				inbox.getId ()));

		if (fromChatUser.getNumber ().getNetwork ().getId () == 0) {

			log.debug (
				stringFormat (
					"message %d: network unknown, ignoring",
					inbox.getId ()));

			return Optional.of (
				inboxLogic.inboxNotProcessed (
					smsMessage,
					Optional.of (
						serviceHelper.findByCode (
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							fromChatUser)),
					Optional.of (
						command),
					stringFormat (
						"network unknown")));

		}

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				fromChatUser,
				true,
				smsMessage.getThreadId ());

		if (creditCheckResult.failed ()) {

			log.debug (
				stringFormat (
					"message %d: ",
					inbox.getId (),
					"credit check failed, sending to help"));

			chatHelpLogLogic.createChatHelpLogIn (
				fromChatUser,
				smsMessage,
				rest,
				null,
				true);

			return Optional.of (
				inboxLogic.inboxProcessed (
					smsMessage,
					Optional.of (
						serviceHelper.findByCode (
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							fromChatUser)),
					command));

		}

		log.debug (
			stringFormat (
				"message %d: ",
				inbox.getId (),
				"not performing credit check"));

		return Optional.<InboxAttemptRec>absent ();

	}

}
