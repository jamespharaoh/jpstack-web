package wbs.apn.chat.contact.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.isoDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.apn.chat.approval.model.ChatApprovalRegexpObjectHelper;
import wbs.apn.chat.approval.model.ChatApprovalRegexpRec;
import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.contact.model.ChatBlockObjectHelper;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserOperatorLabel;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.integrations.jigsaw.api.JigsawApi;
import wbs.integrations.urbanairship.logic.UrbanAirshipApi;
import wbs.platform.media.model.MediaRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.MessageSplitter;

@Log4j
@SingletonComponent ("chatMessageLogic")
public
class ChatMessageLogicImplementation
	implements ChatMessageLogic {

	// dependencies

	@SingletonDependency
	ChatApprovalRegexpObjectHelper chatApprovalRegexpHelper;

	@SingletonDependency
	ChatBlockObjectHelper chatBlockHelper;

	@SingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpLogic chatHelpLogic;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatHelpTemplateLogic chatTemplateLogic;

	@SingletonDependency
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@SingletonDependency
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UrbanAirshipApi urbanAirshipApi;

	// implementation

	@Override
	public
	boolean chatMessageIsRecentDupe (
			ChatUserRec fromUser,
			ChatUserRec toUser,
			TextRec originalText) {

		Transaction transaction =
			database.currentTransaction ();

		Instant oneHourAgo =
			transaction.now ()
				.minus (Duration.standardHours (1));

		List<ChatMessageRec> dupes =
			chatMessageHelper.search (
				new ChatMessageSearch ()

			.fromUserId (
				fromUser.getId ())

			.toUserId (
				toUser.getId ())

			.timestampAfter (
				oneHourAgo)

			.originalTextId (
				originalText.getId ()));

		return dupes.size () > 0;

	}

	@Override
	public
	String chatMessageSendFromUser (
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser,
			@NonNull String text,
			@NonNull Optional <Long> threadId,
			@NonNull ChatMessageMethod source,
			@NonNull List <MediaRec> medias) {

		log.debug (
			stringFormat (
				"chatMessageSendFromUser (%s)",
				joinWithCommaAndSpace (
					objectManager.objectPathMini (
						fromUser),
					objectManager.objectPathMini (
						toUser),
					stringFormat (
						"\"%s\"",
						text.length () > 20
							? text.substring (0, 20)
							: text),
					threadId.isPresent ()
						? threadId.get ().toString ()
						: "null",
					source.toString (),
					integerToDecimalString (
						collectionSize (
							medias)))));

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			fromUser.getChat ();

		// ignore messages from barred users

		ChatCreditCheckResult fromCreditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				fromUser,
				true,
				threadId);

		if (fromCreditCheckResult.failed ()) {

			String errorMessage =
				stringFormat (
					"Ignoring message from barred user %s (%s)",
					fromUser.getCode (),
					fromCreditCheckResult.details ());

			log.info (
				errorMessage);

			return errorMessage;

		}

		TextRec originalText =
			textHelper.findOrCreate (text);

		// ignored duplicated messages

		if (allOf (

			() -> enumNotEqualSafe (
				source,
				ChatMessageMethod.iphone),

			() -> chatMessageIsRecentDupe (
				fromUser,
				toUser,
				originalText)

		)) {

			String errorMessage =
				stringFormat (
					"Ignoring duplicated message from %s to %s, threadId = %s",
					fromUser.getCode (),
					toUser.getCode (),
					threadId.isPresent ()
						? threadId.get ().toString ()
						: "null");

			log.info (
				errorMessage);

			return errorMessage;

		}

		String logMessage =
			stringFormat (
				"Sending user message from %s to %s: %s",
				fromUser.getCode (),
				toUser.getCode (),
				text);

		log.info (
			logMessage);

		// check if the message should be blocked

		ChatBlockRec chatBlock =
			chatBlockHelper.find (
				toUser,
				fromUser);

		ChatCreditCheckResult toCreditCheckResult =
			chatCreditLogic.userCreditCheck (
				toUser);

		boolean blocked =
			chatBlock != null
			|| toCreditCheckResult.failed ();

		// update chat user stats

		fromUser

			.setLastSend (
				transaction.now ())

			.setLastAction (
				transaction.now ());

		// reschedule the next ad

		chatUserLogic.scheduleAd (
			fromUser);

		// clear an alarm if appropriate

		ChatUserAlarmRec alarm =
			chatUserAlarmHelper.find (
				fromUser,
				toUser);

		if (

			alarm != null

			&& earlierThan (
				alarm.getResetTime (),
				transaction.now ())

			&& not (
				alarm.getSticky ())

		) {

			chatUserAlarmHelper.remove (
				alarm);

			chatUserInitiationLogHelper.insert (
				chatUserInitiationLogHelper.createInstance ()

				.setChatUser (
					fromUser)

				.setMonitorChatUser (
					toUser)

				.setReason (
					ChatUserInitiationReason.alarmCancel)

				.setTimestamp (
					transaction.now ())

				.setAlarmTime (
					alarm.getAlarmTime ()));

		}

		// reschedule next outbound

		fromUser

			.setNextQuietOutbound (
				transaction.now ().plus (
					Duration.standardSeconds (
						fromUser.getChat ().getTimeQuietOutbound ())));

		// unschedule any join outbound

		fromUser
			.setNextJoinOutbound (null);

		// cancel previous signup message if there is one

		if (fromUser.getFirstJoin () == null) {

			ChatMessageRec oldMessage =
				chatMessageHelper.findSignup (
					fromUser);

			if (oldMessage != null) {

				log.info (
					stringFormat (
						"Cancelling previously queued message %d",
						integerToDecimalString (
							oldMessage.getId ())));

				oldMessage
					.setStatus (ChatMessageStatus.signupReplaced);

			}

		}

		// create the chat message

		ChatMessageRec chatMessage =
			chatMessageHelper.insert (
				chatMessageHelper.createInstance ()

			.setChat (
				fromUser.getChat ())

			.setFromUser (
				fromUser)

			.setToUser (
				toUser)

			.setTimestamp (
				transaction.now ())

			.setThreadId (
				threadId.orNull ())

			.setOriginalText (
				originalText)

			.setStatus (
				blocked
					? ChatMessageStatus.blocked
					: fromUser.getFirstJoin () == null
						? ChatMessageStatus.signup
						: ChatMessageStatus.sent)

			.setSource (
				source)

			.setMedias (
				medias)

		);

		// check if we should actually send the message

		if (chatMessage.getStatus () == ChatMessageStatus.sent) {

			chatMessageSendFromUserPartTwo (
				chatMessage);

		}

		// if necessary, send a join hint

		if (

			fromUser.getFirstJoin () != null

			&& ! fromUser.getOnline ()

			&& (

				fromUser.getLastJoinHint () == null

				|| fromUser.getLastJoinHint ()
					.plus (
						Duration.standardHours (12))
					.isBefore (
						transaction.now ())

			)

			&& source == ChatMessageMethod.sms

		) {

			chatSendLogic.sendSystemMagic (
				fromUser,
				threadId,
				"logon_hint",
				commandHelper.findByCodeRequired (
					chat,
					"magic"),
				IdObject.objectId (
					commandHelper.findByCodeRequired (
						chat,
						"help")),
				TemplateMissing.error,
				Collections.emptyMap ());

			fromUser

				.setLastJoinHint (
					transaction.now ());

		}

		return null;

	}

	@Override
	public
	void chatMessageSendFromUserPartTwo (
			ChatMessageRec chatMessage) {

		Transaction transaction =
			database.currentTransaction ();

		ChatUserRec fromUser =
			chatMessage.getFromUser ();

		ChatUserRec toUser =
			chatMessage.getToUser ();

		ChatRec chat =
			chatMessage.getChat ();

		ChatContactRec chatUserContact =
			chatContactHelper.findOrCreate (
				fromUser,
				toUser);

		// update stats

		if (toUser.getType () == ChatUserType.user) {

			chatCreditLogic.userSpend (
				fromUser,
				1,
				0,
				0,
				0,
				0);

		} else {

			chatCreditLogic.userSpend (
				fromUser,
				0,
				1,
				0,
				0,
				0);

		}

		toUser

			.setLastReceive (
				transaction.now ());

		// subtract credit etc

		chatCreditLogic.userReceiveSpend (
			toUser,
			1);

		// work out adult state of users

		boolean fromUserAdult =

			fromUser.getAdultVerified ()

			|| enumInSafe (
				chatMessage.getSource (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web,
				ChatMessageMethod.api);

		boolean toUserAdult =

			toUser.getAdultVerified ()

			|| enumEqualSafe (
				toUser.getType (),
				ChatUserType.monitor)

			|| enumInSafe (
				toUser.getDeliveryMethod (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web);

		// if they are both adult

		if (fromUserAdult && toUserAdult) {

			// just send it

			chatMessage.setEditedText (
				chatMessage.getOriginalText ());

			chatMessageDeliver (
				chatMessage);

			chatUserContact

				.setLastDeliveredMessageTime (
					transaction.now ());

		// if either are not adult

		} else {

			// check for approval

			ApprovalResult approvalResult =
				checkForApproval (
					chatMessage.getChat (),
					chatMessage.getOriginalText ().getText ());

			chatMessage.setEditedText (
				textHelper.findOrCreate (
					approvalResult.message));

			switch (approvalResult.status) {

			// is clean
			case clean:

				chatMessage

					.setStatus (
						ChatMessageStatus.sent);

				chatMessageDeliver (
					chatMessage);

				chatUserContact

					.setLastDeliveredMessageTime (
						transaction.now ());

				break;

			// requires automatic editing
			case auto:

				chatMessage

					.setStatus (
						ChatMessageStatus.autoEdited);

				chatMessageDeliver (
					chatMessage);

				chatUserContact

					.setLastDeliveredMessageTime (
						transaction.now ());

				chatUserRejectionCountInc (
					fromUser,
					chatMessage.getThreadId ());

				chatUserRejectionCountInc (
					toUser,
					chatMessage.getThreadId ());

				break;

			// requires manual approval

			case manual:

				chatMessage

					.setStatus (
						ChatMessageStatus.moderatorPending);

				QueueItemRec queueItem =
					queueLogic.createQueueItem (
						queueLogic.findQueue (chat, "message"),
						chatUserContact,
						chatMessage,
						chatUserLogic.getPrettyName (
							fromUser),
						chatMessage.getOriginalText ().getText ());

				chatMessage

					.setQueueItem (
						queueItem);

				break;

			default:

				throw new RuntimeException ();

			}

		}

	}

	@Override
	public
	void chatMessageDeliver (
			ChatMessageRec chatMessage) {

		switch (chatMessage.getToUser ().getType ()) {

		case user:

			chatMessageDeliverToUser (chatMessage);

			break;

		case monitor:

			ChatMonitorInboxRec inbox =
				findOrCreateChatMonitorInbox (
					chatMessage.getToUser (),
					chatMessage.getFromUser (),
					false);

			inbox.setInbound (true);

			break;

		default:

			throw new RuntimeException ();

		}

	}

	@Override
	public
	ApprovalResult checkForApproval (
			@NonNull ChatRec chat,
			@NonNull String message) {

		ApprovalResult approvalResult =
			new ApprovalResult ();

		approvalResult.status =
			ApprovalResult.Status.clean;

		// if this scheme doesn't use approvals, skip it
		// if (! scheme.getApprovals ()) {
		// ret.message = message;
		// return ret;
		// }

		// get and iterate regexps

		List<ChatApprovalRegexpRec> chatApprovalRegexps =
			chatApprovalRegexpHelper.findByParent (
				chat);

		for (
			ChatApprovalRegexpRec chatApprovalRegexp
				: chatApprovalRegexps
		) {

			// look for any matches

			Pattern pattern =
				Pattern.compile (
					chatApprovalRegexp.getRegexp (),
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

			Matcher matcher =
				pattern.matcher (message);

			if (! matcher.find ())
				continue;

			// update the return status as appropriate

			if (
				chatApprovalRegexp.getAuto ()
				&& approvalResult.status == ApprovalResult.Status.clean
			) {

				approvalResult.status =
					ApprovalResult.Status.auto;

			} else {

				approvalResult.status =
					ApprovalResult.Status.manual;

			}

			// now do the replacement

			StringBuffer stringBuffer =
				new StringBuffer ();

			matcher.reset ();

			while (matcher.find ()) {

				matcher.appendReplacement (
					stringBuffer,
					repeatChar (
						'*',
						matcher.group ().length ()));

			}

			matcher.appendTail (
				stringBuffer);

			message =
				stringBuffer.toString ();

		}

		// and return

		approvalResult.message =
			message;

		return approvalResult;

	}

	private
	String repeatChar (
			char ch,
			int len) {

		char[] buf =
			new char [len];

		Arrays.fill (
			buf,
			ch);

		return new String (buf);

	}

	@Override
	public
	boolean chatMessageDeliverToUser (
			@NonNull ChatMessageRec chatMessage) {

		ChatUserRec toUser =
			chatMessage.getToUser ();

		String text =
			chatMessagePrependWarning (
				chatMessage);

		// set the delivery method

		chatMessage.setMethod (
			chatMessage.getToUser ().getDeliveryMethod ());

		// set the delivery id

		if (toUser.getLastDeliveryId () == null)
			toUser.setLastDeliveryId (0l);

		toUser

			.setLastDeliveryId (
				toUser.getLastDeliveryId () + 1);

		chatMessage

			.setDeliveryId (
				toUser.getLastDeliveryId ());

		// delegate as appropriate

		switch (toUser.getDeliveryMethod ()) {

		case sms:

			return chatMessageDeliverViaSms (
				chatMessage,
				text);

		case iphone:

			if (toUser.getJigsawToken () != null) {

				return chatMessageDeliverViaJigsaw (
					chatMessage,
					text);

			}

			return true;

		case web:

			return true;

		default:

			throw new RuntimeException (
				stringFormat (
					"No delivery method for user %s",
					integerToDecimalString (
						toUser.getId ())));

		}
	}

	@Override
	public
	boolean chatMessageDeliverViaJigsaw (
			ChatMessageRec chatMessage,
			String text) {

		ChatUserRec fromUser =
			chatMessage.getFromUser ();

		ChatUserRec toUser =
			chatMessage.getToUser ();

		ChatRec chat =
			toUser.getChat ();

		// count pending allMessages

		List<ChatMessageRec> messages =
			chatMessageHelper.search (
				new ChatMessageSearch ()

			.toUserId (
				toUser.getId ())

			.method (
				ChatMessageMethod.iphone)

			.statusIn (
				ImmutableSet.of (
					ChatMessageStatus.sent,
					ChatMessageStatus.moderatorApproved,
					ChatMessageStatus.moderatorAutoEdited,
					ChatMessageStatus.moderatorEdited))

			.deliveryIdGreaterThan (
				toUser.getLastMessagePollId ())

			.orderBy (
				ChatMessageSearch.Order.deliveryId)

		);

		@SuppressWarnings ("unused")
		JigsawApi.PushRequest jigsawRequest =
			new JigsawApi.PushRequest ()

			.applicationIdentifier (
				chat.getJigsawApplicationIdentifier ())

			.addToken (
				toUser.getJigsawToken ())

			.messageBody (
				stringFormat (
					"New message from %s",
					ifNull (
						fromUser.getName (),
						fromUser.getCode ())))

			.messageSound (
				"default")

			.addMessageCustomProperty (
				"fromUserCode",
				fromUser.getCode ())

			.addMessageCustomProperty (
				"timestamp",
				isoDate (
					chatMessage.getTimestamp ()))

			.messageBadge (
				messages.size ())

			.messageSound (
				"default");

		boolean success = true;

		long jigsawStart = System.nanoTime ();

		try {

			//jigsawApi.pushServer (
				//jigsawRequest);

		} catch (Exception exception) {

			exceptionLogger.logSimple (
				"unknown",
				"ChatLogicImpl.chatMessageDeliverViaJigsaw (...)",

				"JigsawApi.pushServer threw exception",
				"Chat message id: " + chatMessage.getId () + "\n" +
				"\n" +
				exceptionLogic.throwableDump (
					exception),

				Optional.absent (),
				GenericExceptionResolution.ignoreWithNoWarning);

			success = false;

		}

		long jigsawEnd =
			System.nanoTime ();

		log.info (
			"Call to jigsaw took " + (jigsawEnd - jigsawStart) + "ns");

		UrbanAirshipApi.PushRequest urbanApiRequest =
			new UrbanAirshipApi.PushRequest ();

		urbanApiRequest.tokens.add (
			toUser.getJigsawToken ());

		urbanApiRequest.apsAlert =
			"New message from " + ifNull (
				fromUser.getName (),
				fromUser.getCode ());

		urbanApiRequest.apsSound =
			"default";

		urbanApiRequest.apsBadge =
			messages.size ();

		urbanApiRequest.customProperties.put (
			"fromUserCode",
			fromUser.getCode ());

		urbanApiRequest.customProperties.put (
			"timestamp",
			isoDate (
				chatMessage.getTimestamp ()));

		long urbanStart =
			System.nanoTime ();

		try {

			String accountKey =
				toUser.getChatAffiliate ().getId ().toString ();

			urbanAirshipApi.push (
				accountKey,
				"prod",
				urbanApiRequest);

		} catch (Exception exception) {

			exceptionLogger.logSimple (
				"unknown",
				"ChatLogicImpl.chatMessageDeliverViaJigsaw (...)",

				"JigsawApi.pushServer threw exception",
				"Chat message id: " + chatMessage.getId () + "\n" +
				"\n" +
				exceptionLogic.throwableDump (
					exception),

				Optional.absent (),
				GenericExceptionResolution.ignoreWithNoWarning);

			success = false;

		}

		long urbanEnd =
			System.nanoTime ();

		log.info (
			stringFormat (
				"Call to urban airship (prod) took %sns",
				integerToDecimalString (
					urbanEnd - urbanStart)));

		urbanStart = System.nanoTime ();
		try {

			String accountKey =
				toUser.getChatAffiliate ().getId ().toString ();

			urbanAirshipApi.push (
				accountKey,
				"dev",
				urbanApiRequest);

		} catch (Exception exception) {

			exceptionLogger.logSimple (
				"unknown",
				"ChatLogicImpl.chatMessageDeliverViaJigsaw (...)",

				"JigsawApi.pushServer threw exception",
				"Chat message id: " + chatMessage.getId () + "\n" +
				"\n" +
				exceptionLogic.throwableDump (
					exception),

				Optional.absent (),
				GenericExceptionResolution.ignoreWithNoWarning);

			success = false;

		}

		urbanEnd =
			System.nanoTime ();

		log.info (
			stringFormat (
				"Call to urban airship (dev) took %sns",
				integerToDecimalString (
					urbanEnd - urbanStart)));

		return success;

	}

	@Override
	public
	ChatMonitorInboxRec findOrCreateChatMonitorInbox (
			@NonNull ChatUserRec monitorChatUser,
			@NonNull ChatUserRec userChatUser,
			boolean alarm) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			userChatUser.getChat ();

		// lookup chat monitor inbox

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.find (
				monitorChatUser,
				userChatUser);

		if (chatMonitorInbox != null)
			return chatMonitorInbox;

		// there wasn't one, create one

		chatMonitorInbox =
			chatMonitorInboxHelper.insert (
				chatMonitorInboxHelper.createInstance ()

			.setMonitorChatUser (
				monitorChatUser)

			.setUserChatUser (
				userChatUser)

			.setTimestamp (
				transaction.now ())

		);

		// find chat user contact

		ChatContactRec chatUserContact =
			chatContactHelper.findOrCreate (
				userChatUser,
				monitorChatUser);

		// check which queue to use

		Gender monitorGender =
			monitorChatUser.getGender ();

		Gender userGender =
			userChatUser.getGender ();

		String queueCode;

		if (
			monitorGender == null
			|| userGender == null
		) {

			queueCode =
				"chat_unknown";

		} else {

			String sexuality =
				monitorGender == userGender
					? "gay"
					: "straight";

			String gender =
				monitorGender.toString ();

			queueCode =
				stringFormat (
					"chat_%s_%s",
					sexuality,
					gender);

		}

		if (alarm) {

			queueCode +=
				"_alarm";

		}

		QueueRec queue =
			queueLogic.findQueue (
				chat,
				queueCode);

		// and create the queue item

		QueueItemRec queueItem =
			queueLogic.createQueueItem (
				queue,
				chatUserContact,
				chatMonitorInbox,
				userChatUser.getCode (),
				"");

		chatMonitorInbox

			.setQueueItem (
				queueItem);

		// and return

		return chatMonitorInbox;

	}

	@Override
	public
	boolean chatMessageDeliverViaSms (
			@NonNull ChatMessageRec chatMessage,
			@NonNull String text) {

		ChatUserRec fromUser =
			chatMessage.getFromUser ();

		ChatUserRec toUser =
			chatMessage.getToUser ();

		ChatRec chat =
			toUser.getChat ();

		List<String> stringParts;

		try {

			stringParts =
				MessageSplitter.split (
					text,
					messageFromTemplates (
						fromUser));

		} catch (IllegalArgumentException exception) {

			log.error (
				"MessageSplitter.split threw exception",
				exception);

			exceptionLogger.logSimple (
				"unknown",
				"ChatReceivedHandler.sendUserMessage (...)",
				"MessageSplitter.split (...) threw IllegalArgumentException",

				stringFormat (

					"Error probably caused by illegal characters in message. ",
					" Ignoring error.\n",

					"\n",

					"fromUser.id = %s\n",
					integerToDecimalString (
						fromUser.getId ()),

					"toUser.id = %s\n",
					integerToDecimalString (
						toUser.getId ()),

					"text = '%s'\n",
					text,

					"\n",

					"%s",
					exceptionLogic.throwableDump (
						exception)),

				Optional.absent (),
				GenericExceptionResolution.ignoreWithNoWarning);

			return false;

		}

		// and send the message(s)

		String serviceCode =
			chatMessage.getFromUser ().getType ().toString ();

		List<TextRec> textParts =
			new ArrayList<TextRec> ();

		for (
			String part
				: stringParts
		) {

			textParts.add (
				textHelper.findOrCreate (
					part));

		}

		Long threadId =
			chatSendLogic.sendMessageMagic (
				toUser,
				Optional.fromNullable (
					chatMessage.getThreadId ()),
				textParts,
				commandHelper.findByCodeRequired (
					chat,
					"chat"),
				serviceHelper.findByCodeRequired (
					chat,
					serviceCode),
				fromUser.getId (),
				Optional.fromNullable (
					chatMessage.getSender ()));

		chatMessage

			.setThreadId (
				threadId)

			.setMethod (
				ChatMessageMethod.sms);

		return true;

	}

	@Override
	public
	String chatMessagePrependWarning (
			ChatMessageRec chatMessage) {

		String text =
			chatMessage.getEditedText ().getText ();

		// prepend a warning if appropriate

		if (chatMessage.getMonitorWarning ()) {

			ChatUserRec chatUser =
				chatMessage.getToUser ();

			ChatHelpTemplateRec template =
				chatTemplateLogic.findChatHelpTemplate (
					chatUser,
					"system",
					warningByOperatorLabel.get (
						chatUser.getOperatorLabel ()));

			text =
				template.getText () + text;

		}

		return text;

	}

	static
	Map<ChatUserOperatorLabel,String> warningByOperatorLabel =
		ImmutableMap.<ChatUserOperatorLabel,String>builder ()

		.put (
			ChatUserOperatorLabel.operator,
			"operator_message_warning")

		.put (
			ChatUserOperatorLabel.monitor,
			"monitor_message_warning")

		.build ();

	/**
	 * Increments a chat users rejection count and, when appropriate, triggers
	 * the adult verification as appropriate depending on their network.
	 *
	 * This may be called for already verified users, and monitors (as both
	 * users in a user-to-user message must be verified and this method is still
	 * called for both of them). As such we check for them and skip the
	 * verification process.
	 *
	 * @param chatUser
	 *            ChatUserRec of chat user to inc count of
	 * @param threadId
	 *            threadId of existing message thread to associate messages with
	 */
	@Override
	public
	void chatUserRejectionCountInc (
			ChatUserRec chatUser,
			Long threadId) {

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// ignore iphones

		if (chatUser.getDeliveryMethod () == ChatMessageMethod.iphone)
			return;

		// increment their rejection count

		chatUser.setRejectionCount (
			chatUser.getRejectionCount () + 1);

		// ignore monitors

		if (chatUser.getType () == ChatUserType.monitor)
			return;

		// ignore already verified users

		if (chatUser.getAdultVerified ())
			return;

		// ignore them except the first and every fifth thereafter

		if (chatUser.getRejectionCount () % 5 != 1)
			return;

		// send the hint explaining how to adult verify

		chatSendLogic.sendSystem (
			chatUser,
			Optional.fromNullable (
				threadId),
			"adult_hint_in",
			chatScheme.getRbFreeRouter (),
			"89505",
			Collections.<String>emptySet (),
			Optional.<String>absent (),
			"system",
			TemplateMissing.error,
			Collections.<String,String>emptyMap ());

	}

	private final static
	MessageSplitter.Templates messageFromTemplates (
			@NonNull ChatUserRec chatUser) {

		String userId =
			chatUser.getName () == null
				? chatUser.getCode ()
				: stringFormat (
					"%s %s",
					chatUser.getName (),
					chatUser.getCode ());

		return new MessageSplitter.Templates (

			stringFormat (
				"From %s: {message}",
				userId),

			stringFormat (
				"From %s {page}/{pages}: {message}",
				userId),

			stringFormat (
				"From %s {page}/{pages}: {message}",
				userId),

			stringFormat (
				"From %s {page}/{pages}: {message}",
				userId));

	}

}
