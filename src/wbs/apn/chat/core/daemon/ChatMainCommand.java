package wbs.apn.chat.core.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.logging.LogContext;

import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.DateFinder;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
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

/**
 * MainCommandHandler takes input from the main chat interface, looking for
 * keywords or box numbers and forwarding to the appropriate command.
 */
@Accessors (fluent = true)
@PrototypeComponent ("chatMainCommand")
public
class ChatMainCommand
	implements CommandHandler {

	// dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatKeywordObjectHelper chatKeywordHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatSchemeKeywordObjectHelper chatSchemeKeywordHelper;

	@SingletonDependency
	ChatSchemeObjectHelper chatSchemeHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@WeakSingletonDependency
	CommandManager commandManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	KeywordFinder keywordFinder;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatJoiner> chatJoinerProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

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

	// public implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handle");

		) {

			transaction.debugFormat (
				"message %s: begin processing",
				integerToDecimalString (
					inbox.getId ()));

			commandChatScheme =
				chatSchemeHelper.findRequired (
					transaction,
					command.getParentId ());

			chat =
				commandChatScheme.getChat ();

			smsMessage =
				inbox.getMessage ();

			fromChatUser =
				chatUserHelper.findOrCreate (
					transaction,
					chat,
					smsMessage);

			transaction.debugFormat (
				"message %s: full text \"%s\"",
				integerToDecimalString (
					inbox.getId ()),
				smsMessage.getText ().getText ());

			transaction.debugFormat (
				"message %s: rest \"%s\"",
				integerToDecimalString (
					inbox.getId ()),
				rest);

			// set chat scheme and adult verify

			chatUserLogic.setScheme (
				transaction,
				fromChatUser,
				commandChatScheme);

			if (
				smsMessage.getRoute ().getInboundImpliesAdult ()
				|| chat.getAutoAdultVerify ()
			) {

				chatUserLogic.adultVerify (
					transaction,
					fromChatUser);

			}

			// look for a date of birth

			Optional <InboxAttemptRec> dobInboxAttempt =
				tryDob (
					transaction);

			if (dobInboxAttempt.isPresent ()) {
				return dobInboxAttempt.get ();
			}

			// look for a keyword

			for (
				KeywordFinder.Match match
					: keywordFinder.find (
						rest)
			) {

				transaction.debugFormat (
					"message %s: trying keyword \"%s\"",
					integerToDecimalString (
						inbox.getId ()),
					match.simpleKeyword ());

				// check if the keyword is a 6-digit number

				if (match.simpleKeyword ().matches ("\\d{6}")) {

					return doCode (
						transaction,
						match.simpleKeyword (),
						match.rest ());

				}

				// check if it's a chat keyword

				Optional <InboxAttemptRec> keywordInboxAttempt =
					tryKeyword (
						transaction,
						match.simpleKeyword (),
						match.rest ());

				if (keywordInboxAttempt.isPresent ())
					return keywordInboxAttempt.get ();

			}

			// no keyword found

			if (fromChatUser.getLastJoin () == null) {

				if (chat.getErrorOnUnrecognised ()) {

					transaction.debugFormat (
						"message %s: ",
						integerToDecimalString (
							inbox.getId ()),
						"no keyword found, new user, sending error");

					chatHelpLogLogic.createChatHelpLogIn (
						transaction,
						fromChatUser,
						smsMessage,
						rest,
						optionalAbsent (),
						false);

					chatSendLogic.sendSystemRbFree (
						transaction,
						fromChatUser,
						optionalOf (
							smsMessage.getThreadId ()),
						"keyword_error",
						TemplateMissing.error,
						emptyMap ());

					return smsInboxLogic.inboxProcessed (
						transaction,
						inbox,
						optionalOf (
							serviceHelper.findByCodeRequired (
								transaction,
								chat,
								"default")),
						optionalOf (
							chatUserLogic.getAffiliate (
								transaction,
								fromChatUser)),
						command);

				} else {

					transaction.debugFormat (
						"message %s: ",
						integerToDecimalString (
							inbox.getId ()),
						"no keyword found, new user, joining");

					return chatJoinerProvider.get ()

						.chatId (
							chat.getId ())

						.joinType (
							JoinType.chatSimple)

						.chatSchemeId (
							commandChatScheme.getId ())

						.inbox (
							inbox)

						.rest (
							rest)

						.handleInbox (
							transaction,
							command);

				}

			} else {

				transaction.debugFormat (
					"message %s: ",
					integerToDecimalString (
						inbox.getId ()),
					"no keyword found, existing user, sent to help");

				chatHelpLogLogic.createChatHelpLogIn (
					transaction,
					fromChatUser,
					smsMessage,
					rest,
					optionalAbsent (),
					true);

				return smsInboxLogic.inboxProcessed (
					transaction,
					inbox,
					optionalOf (
						serviceHelper.findByCodeRequired (
							transaction,
							chat,
							"default")),
					optionalOf (
						chatUserLogic.getAffiliate (
							transaction,
							fromChatUser)),
					command);

			}

		}

	}

	// private implementation

	InboxAttemptRec doCode (
			@NonNull Transaction parentTransaction,
			@NonNull String code,
			@NonNull String rest) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"doCode");

		) {

			Optional <ChatUserRec> toUserOptional =
				chatUserHelper.findByCode (
					transaction,
					chat,
					code);

			ChatSchemeRec userChatScheme =
				fromChatUser.getChatScheme ();

			if (
				optionalIsNotPresent (
					toUserOptional)
			) {

				transaction.debugFormat (
					"message %s: ignoring invalid user code %s",
					integerToDecimalString (
						inbox.getId ()),
					code);

				return smsInboxLogic.inboxProcessed (
					transaction,
					inbox,
					Optional.of (
						serviceHelper.findByCodeRequired (
							transaction,
							chat,
							"default")),
					Optional.of (
						chatUserLogic.getAffiliate (
							transaction,
							fromChatUser)),
					command);

			}

			ChatUserRec toUser =
				toUserOptional.get ();

			transaction.debugFormat (
				"message %s: message to user %s",
				integerToDecimalString (
					inbox.getId ()),
				integerToDecimalString (
					toUser.getId ()));

			chatMessageLogic.chatMessageSendFromUser (
				transaction,
				fromChatUser,
				toUser,
				rest,
				Optional.of (
					smsMessage.getThreadId ()),
				ChatMessageMethod.sms,
				Collections.<MediaRec>emptyList ());

			// send signup info if relevant

			if (fromChatUser.getFirstJoin () == null) {

				chatSendLogic.sendSystem (
					transaction,
					fromChatUser,
					optionalOf (
						smsMessage.getThreadId ()),
					"message_signup",
					userChatScheme.getRbFreeRouter (),
					userChatScheme.getRbNumber (),
					Collections.<String>emptySet (),
					optionalAbsent (),
					"system",
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

				chatSendLogic.sendSystemMagic (
					transaction,
					fromChatUser,
					Optional.of (
						smsMessage.getThreadId ()),
					"dob_request",
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"magic"),
					IdObject.objectId (
						commandHelper.findByCodeRequired (
							transaction,
							userChatScheme,
							"chat_dob")),
					TemplateMissing.error,
					Collections.emptyMap ());

			}

			return smsInboxLogic.inboxProcessed (
				transaction,
				inbox,
				optionalOf (
					serviceHelper.findByCodeRequired (
						transaction,
						chat,
						"default")),
				optionalOf (
					chatUserLogic.getAffiliate (
						transaction,
						fromChatUser)),
				command);

		}

	}

	Optional <InboxAttemptRec> trySchemeKeyword (
			@NonNull Transaction parentTransaction,
			@NonNull String keyword,
			@NonNull String rest) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"trySchemeKeyword");

		) {

			Optional <ChatSchemeKeywordRec> chatSchemeKeywordOptional =
				chatSchemeKeywordHelper.findByCode (
					transaction,
					commandChatScheme,
					keyword);

			if (
				optionalIsNotPresent (
					chatSchemeKeywordOptional)
			) {

				transaction.debugFormat (
					"message %s: no chat scheme keyword \"%s\"",
					integerToDecimalString (
						inbox.getId ()),
					keyword);

				return optionalAbsent ();

			}

			ChatSchemeKeywordRec chatSchemeKeyword =
				chatSchemeKeywordOptional.get ();

			if (chatSchemeKeyword.getJoinType () != null) {

				transaction.debugFormat (
					"message %s: chat scheme keyword \"%s\" is join type %s",
					integerToDecimalString (
						inbox.getId ()),
					keyword,
					chatSchemeKeyword.getJoinType ().toString ());

				if (! chatSchemeKeyword.getNoCreditCheck ()) {

					Optional <InboxAttemptRec> inboxAttempt =
						performCreditCheck (
							transaction);

					if (inboxAttempt.isPresent ())
						return inboxAttempt;

				}

				Long chatAffiliateId =
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

					.inbox (
						inbox)

					.rest (
						rest)

					.handleInbox (
						transaction,
						command)

				);

			}

			if (chatSchemeKeyword.getCommand () != null) {

				transaction.debugFormat (
					"message %s: ",
					integerToDecimalString (
						inbox.getId ()),
					"chat scheme keyword \"%s\" ",
					keyword,
					"is command %s",
					integerToDecimalString (
						chatSchemeKeyword.getCommand ().getId ()));

				if (! chatSchemeKeyword.getNoCreditCheck ()) {

					Optional<InboxAttemptRec> inboxAttempt =
						performCreditCheck (
							transaction);

					if (inboxAttempt.isPresent ())
						return inboxAttempt;

				}

				return Optional.of (
					commandManager.handle (
						transaction,
						inbox,
						chatSchemeKeyword.getCommand (),
						optionalAbsent (),
						rest));

			}

			// this keyword does nothing?

			transaction.warningFormat (
				"message %s: chat scheme keyword \"%s\" does nothing",
				integerToDecimalString (
					inbox.getId ()),
				keyword);

			return optionalAbsent ();

		}

	}

	private
	Optional <InboxAttemptRec> tryChatKeyword (
			@NonNull Transaction parentTransaction,
			@NonNull String keyword,
			@NonNull String rest) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"tryChatKeyword");

		) {

			Optional <ChatKeywordRec> chatKeywordOptional =
				chatKeywordHelper.findByCode (
					transaction,
					chat,
					keyword);

			if (
				optionalIsNotPresent (
					chatKeywordOptional)
			) {

				transaction.debugFormat (
					"message %s: no chat keyword \"%s\"",
					integerToDecimalString (
						inbox.getId ()),
					keyword);

				return optionalAbsent ();

			}

			ChatKeywordRec chatKeyword =
				chatKeywordOptional.get ();

			if (chatKeyword.getJoinType () != null) {

				transaction.debugFormat (
					"message %s: ",
					integerToDecimalString (
						inbox.getId ()),
					"chat keyword \"%s\" ",
					keyword,
					"is join type %s",
					chatKeyword.getJoinType ().toString ());

				Long chatAffiliateId =
					chatKeyword.getJoinChatAffiliate () != null
						? chatKeyword.getJoinChatAffiliate ().getId ()
						: null;

				if (! chatKeyword.getNoCreditCheck ()) {

					Optional <InboxAttemptRec> inboxAttempt =
						performCreditCheck (
							transaction);

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

					.inbox (
						inbox)

					.rest (
						rest)

					.handleInbox (
						transaction,
						command)

				);

			}

			if (chatKeyword.getCommand () != null) {

				transaction.debugFormat (
					"message %s: chat keyword \"%s\" is command %s",
					integerToDecimalString (
						inbox.getId ()),
					keyword,
					integerToDecimalString (
						chatKeyword.getCommand ().getId ()));

				return optionalOf (
					commandManager.handle (
						transaction,
						inbox,
						chatKeyword.getCommand (),
						optionalAbsent (),
						rest));

			}

			// this keyword does nothing

			transaction.warningFormat (
				"message %s: ",
				integerToDecimalString (
					inbox.getId ()),
				"chat keyword \"%s\" ",
				keyword,
				"does nothing");

			return optionalAbsent ();

		}

	}

	Optional <InboxAttemptRec> tryKeyword (
			@NonNull Transaction parentTransaction,
			@NonNull String keyword,
			@NonNull String rest) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"tryKeyword");

		) {

			Optional <InboxAttemptRec> schemeKeywordInboxAttempt =
				trySchemeKeyword (
					transaction,
					keyword,
					rest);

			if (schemeKeywordInboxAttempt.isPresent ())
				return schemeKeywordInboxAttempt;

			Optional <InboxAttemptRec> chatKeywordInboxAttempt =
				tryChatKeyword (
					transaction,
					keyword,
					rest);

			if (chatKeywordInboxAttempt.isPresent ())
				return chatKeywordInboxAttempt;

			return optionalAbsent ();

		}

	}

	Optional <InboxAttemptRec> tryDob (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"tryDob");

		) {

			if (fromChatUser.getFirstJoin () != null) {
				return optionalAbsent ();
			}

			if (

				isNotNull (
					fromChatUser.getNextJoinType ())

				&& enumNotInSafe (
					fromChatUser.getNextJoinType (),
					ChatKeywordJoinType.chatDob,
					ChatKeywordJoinType.dateDob)

			) {
				return optionalAbsent ();
			}

			Optional <LocalDate> dateOfBirth =
				DateFinder.find (
					rest,
					1915);

			if (
				optionalIsNotPresent (
					dateOfBirth)
			) {
				return optionalAbsent ();
			}

			return Optional.of (
				chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					JoinType.chatDob)

				.chatSchemeId (
					commandChatScheme.getId ())

				.inbox (
					inbox)

				.rest (
					rest)

				.handleInbox (
					transaction,
					command)

			);

		}

	}

	Optional <InboxAttemptRec> performCreditCheck (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"performCreditCheck");

		) {

			transaction.debugFormat (
				"message %s: performing credit check",
				integerToDecimalString (
					inbox.getId ()));

			if (fromChatUser.getNumber ().getNetwork ().getId () == 0) {

				transaction.debugFormat (
					"message %s: network unknown, ignoring",
					integerToDecimalString (
						inbox.getId ()));

				return optionalOf (
					smsInboxLogic.inboxNotProcessed (
						transaction,
						inbox,
						optionalOf (
							serviceHelper.findByCodeRequired (
								transaction,
								chat,
								"default")),
						optionalOf (
							chatUserLogic.getAffiliate (
								transaction,
								fromChatUser)),
						Optional.of (
							command),
						stringFormat (
							"network unknown")));

			}

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					transaction,
					fromChatUser,
					true,
					optionalOf (
						smsMessage.getThreadId ()));

			if (creditCheckResult.failed ()) {

				transaction.debugFormat (
					"message %s: ",
					integerToDecimalString (
						inbox.getId ()),
					"credit check failed, sending to help");

				chatHelpLogLogic.createChatHelpLogIn (
					transaction,
					fromChatUser,
					smsMessage,
					rest,
					optionalAbsent (),
					true);

				return optionalOf (
					smsInboxLogic.inboxProcessed (
						transaction,
						inbox,
						optionalOf (
							serviceHelper.findByCodeRequired (
								transaction,
								chat,
								"default")),
						optionalOf (
							chatUserLogic.getAffiliate (
								transaction,
								fromChatUser)),
						command));

			}

			transaction.debugFormat (
				"message %s: ",
				integerToDecimalString (
					inbox.getId ()),
				"not performing credit check");

			return optionalAbsent ();

		}

	}

}
