package wbs.apn.chat.contact.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
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

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
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
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

import wbs.utils.time.TextualInterval;

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
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserOperatorLabel;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;

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
	ChatUserObjectHelper chatUserHelper;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	MessageObjectHelper smsMessageHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UrbanAirshipApi urbanAirshipApi;

	// implementation

	@Override
	public
	boolean chatMessageIsRecentDupe (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser,
			@NonNull TextRec originalText) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageIsRecentDupe");

		) {

			Instant oneHourAgo =
				transaction.now ()
					.minus (Duration.standardHours (1));

			List <ChatMessageRec> dupes =
				chatMessageHelper.search (
					transaction,
					new ChatMessageSearch ()

				.fromUserId (
					fromUser.getId ())

				.toUserId (
					toUser.getId ())

				.timestamp (
					TextualInterval.after (
						DateTimeZone.UTC,
						oneHourAgo))

				.originalTextId (
					originalText.getId ())

			);

			return dupes.size () > 0;

		}

	}

	@Override
	public
	String chatMessageSendFromUser (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser,
			@NonNull String text,
			@NonNull Optional <Long> threadId,
			@NonNull ChatMessageMethod source,
			@NonNull List <MediaRec> medias) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageSendFromUser");

		) {

			transaction.debugFormat (
				"chatMessageSendFromUser (%s)",
				joinWithCommaAndSpace (
					objectManager.objectPathMini (
						transaction,
						fromUser),
					objectManager.objectPathMini (
						transaction,
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
							medias))));

			ChatRec chat =
				fromUser.getChat ();

			// ignore messages from barred users

			ChatCreditCheckResult fromCreditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					transaction,
					fromUser,
					true,
					threadId);

			if (fromCreditCheckResult.failed ()) {

				String errorMessage =
					stringFormat (
						"Ignoring message from barred user %s (%s)",
						fromUser.getCode (),
						fromCreditCheckResult.details ());

				transaction.noticeFormat (
					"%s",
					errorMessage);

				return errorMessage;

			}

			TextRec originalText =
				textHelper.findOrCreate (
					transaction,
					text);

			// ignored duplicated messages

			if (

				enumNotEqualSafe (
					source,
					ChatMessageMethod.iphone)

				&& chatMessageIsRecentDupe (
					transaction,
					fromUser,
					toUser,
					originalText)

			) {

				String errorMessage =
					stringFormat (
						"Ignoring duplicated message from %s ",
						fromUser.getCode (),
						"to %s, ",
						toUser.getCode (),
						"threadId = %s",
						threadId.isPresent ()
							? threadId.get ().toString ()
							: "null");

				transaction.noticeFormat (
					"%s",
					errorMessage);

				return errorMessage;

			}

			String logMessage =
				stringFormat (
					"Sending user message from %s to %s: %s",
					fromUser.getCode (),
					toUser.getCode (),
					text);

			transaction.noticeFormat (
				"%s",
				logMessage);

			// check if the message should be blocked

			ChatBlockRec chatBlock =
				chatBlockHelper.find (
					transaction,
					toUser,
					fromUser);

			ChatCreditCheckResult toCreditCheckResult =
				chatCreditLogic.userCreditCheck (
					transaction,
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
				transaction,
				fromUser);

			// clear an alarm if appropriate

			ChatUserAlarmRec alarm =
				chatUserAlarmHelper.find (
					transaction,
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
					transaction,
					alarm);

				chatUserInitiationLogHelper.insert (
					transaction,
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
						transaction,
						fromUser);

				if (oldMessage != null) {

					transaction.noticeFormat (
						"Cancelling previously queued message %s",
						integerToDecimalString (
							oldMessage.getId ()));

					oldMessage

						.setStatus (
							ChatMessageStatus.signupReplaced);

				}

			}

			// create the chat message

			ChatMessageRec chatMessage =
				chatMessageHelper.insert (
					transaction,
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
					transaction,
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
					transaction,
					fromUser,
					threadId,
					"logon_hint",
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"magic"),
					IdObject.objectId (
						commandHelper.findByCodeRequired (
							transaction,
							chat,
							"help")),
					TemplateMissing.error,
					emptyMap ());

				fromUser

					.setLastJoinHint (
						transaction.now ());

			}

			return null;

		}

	}

	@Override
	public
	void chatMessageSendFromUserPartTwo (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageRec chatMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageSendFromUserPartTwo");

		) {

			ChatUserRec fromUser =
				chatMessage.getFromUser ();

			ChatUserRec toUser =
				chatMessage.getToUser ();

			ChatRec chat =
				chatMessage.getChat ();

			ChatContactRec chatUserContact =
				chatContactHelper.findOrCreate (
					transaction,
					fromUser,
					toUser);

			// update stats

			if (toUser.getType () == ChatUserType.user) {

				chatCreditLogic.userSpend (
					transaction,
					fromUser,
					1,
					0,
					0,
					0,
					0);

			} else {

				chatCreditLogic.userSpend (
					transaction,
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
				transaction,
				toUser,
				1l);

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
					transaction,
					chatMessage);

				chatUserContact

					.setLastDeliveredMessageTime (
						transaction.now ());

			// if either are not adult

			} else {

				// check for approval

				ApprovalResult approvalResult =
					checkForApproval (
						transaction,
						chatMessage.getChat (),
						chatMessage.getOriginalText ().getText ());

				chatMessage.setEditedText (
					textHelper.findOrCreate (
						transaction,
						approvalResult.message));

				switch (approvalResult.status) {

				// is clean
				case clean:

					chatMessage

						.setStatus (
							ChatMessageStatus.sent);

					chatMessageDeliver (
						transaction,
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
						transaction,
						chatMessage);

					chatUserContact

						.setLastDeliveredMessageTime (
							transaction.now ());

					chatUserRejectionCountInc (
						transaction,
						fromUser,
						smsMessageHelper.findRequired (
							transaction,
							chatMessage.getThreadId ()));

					chatUserRejectionCountInc (
						transaction,
						toUser,
						smsMessageHelper.findRequired (
							transaction,
							chatMessage.getThreadId ()));

					break;

				// requires manual approval

				case manual:

					chatMessage

						.setStatus (
							ChatMessageStatus.moderatorPending);

					QueueItemRec queueItem =
						queueLogic.createQueueItem (
							transaction,
							chat,
							"message",
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

	}

	@Override
	public
	void chatMessageDeliver (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageRec chatMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageDeliver");

		) {

			switch (chatMessage.getToUser ().getType ()) {

			case user:

				chatMessageDeliverToUser (
					transaction,
					chatMessage);

				break;

			case monitor:

				ChatMonitorInboxRec inbox =
					findOrCreateChatMonitorInbox (
						transaction,
						chatMessage.getToUser (),
						chatMessage.getFromUser (),
						false);

				inbox.setInbound (true);

				break;

			default:

				throw new RuntimeException ();

			}

		}

	}

	@Override
	public
	ApprovalResult checkForApproval (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull String originalMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"checkForApproval");

		) {

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

			List <ChatApprovalRegexpRec> chatApprovalRegexps =
				chatApprovalRegexpHelper.findByParent (
					transaction,
					chat);

			String currentMessage =
				originalMessage;

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
					pattern.matcher (
						originalMessage);

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

				currentMessage =
					stringBuffer.toString ();

			}

			// and return

			approvalResult.message =
				currentMessage;

			return approvalResult;

		}

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
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageRec chatMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageDeliverToUser");

		) {

			ChatUserRec toUser =
				chatMessage.getToUser ();

			String text =
				chatMessagePrependWarning (
					transaction,
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
					transaction,
					chatMessage,
					text);

			case iphone:

				if (toUser.getJigsawToken () != null) {

					return chatMessageDeliverViaJigsaw (
						transaction,
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

	}

	@Override
	public
	boolean chatMessageDeliverViaJigsaw (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageRec chatMessage,
			@NonNull String text) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageDeliverViaJigsaw");

		) {

			ChatUserRec fromUser =
				chatMessage.getFromUser ();

			ChatUserRec toUser =
				chatMessage.getToUser ();

			ChatRec chat =
				toUser.getChat ();

			// count pending allMessages

			List <ChatMessageRec> messages =
				chatMessageHelper.search (
					transaction,
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
					transaction,
					"unknown",
					"ChatLogicImpl.chatMessageDeliverViaJigsaw (...)",

					"JigsawApi.pushServer threw exception",
					"Chat message id: " + chatMessage.getId () + "\n" +
					"\n" +
					exceptionLogic.throwableDump (
						transaction,
						exception),

					optionalAbsent (),
					GenericExceptionResolution.ignoreWithNoWarning);

				success = false;

			}

			long jigsawEnd =
				System.nanoTime ();

			transaction.noticeFormat (
				"Call to jigsaw took %sns",
				integerToDecimalString (
					jigsawEnd - jigsawStart));

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
					transaction,
					accountKey,
					"prod",
					urbanApiRequest);

			} catch (Exception exception) {

				exceptionLogger.logSimple (
					transaction,
					"unknown",
					"ChatLogicImpl.chatMessageDeliverViaJigsaw (...)",

					"JigsawApi.pushServer threw exception",
					"Chat message id: " + chatMessage.getId () + "\n" +
					"\n" +
					exceptionLogic.throwableDump (
						transaction,
						exception),

					optionalAbsent (),
					GenericExceptionResolution.ignoreWithNoWarning);

				success = false;

			}

			long urbanEnd =
				System.nanoTime ();

			transaction.noticeFormat (
				"Call to urban airship (prod) took %sns",
				integerToDecimalString (
					urbanEnd - urbanStart));

			urbanStart = System.nanoTime ();

			try {

				String accountKey =
					toUser.getChatAffiliate ().getId ().toString ();

				urbanAirshipApi.push (
					transaction,
					accountKey,
					"dev",
					urbanApiRequest);

			} catch (Exception exception) {

				exceptionLogger.logSimple (
					transaction,
					"unknown",
					"ChatLogicImpl.chatMessageDeliverViaJigsaw (...)",

					"JigsawApi.pushServer threw exception",
					"Chat message id: " + chatMessage.getId () + "\n" +
					"\n" +
					exceptionLogic.throwableDump (
						transaction,
						exception),

					optionalAbsent (),
					GenericExceptionResolution.ignoreWithNoWarning);

				success = false;

			}

			urbanEnd =
				System.nanoTime ();

			transaction.noticeFormat (
				"Call to urban airship (dev) took %sns",
				integerToDecimalString (
					urbanEnd - urbanStart));

			return success;

		}

	}

	@Override
	public
	ChatMonitorInboxRec findOrCreateChatMonitorInbox (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec monitorChatUser,
			@NonNull ChatUserRec userChatUser,
			boolean alarm) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreateChatMonitorInbox");

		) {

			ChatRec chat =
				userChatUser.getChat ();

			// lookup chat monitor inbox

			ChatMonitorInboxRec chatMonitorInbox =
				chatMonitorInboxHelper.find (
					transaction,
					monitorChatUser,
					userChatUser);

			if (chatMonitorInbox != null)
				return chatMonitorInbox;

			// there wasn't one, create one

			chatMonitorInbox =
				chatMonitorInboxHelper.insert (
					transaction,
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
					transaction,
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
					transaction,
					chat,
					queueCode);

			// and create the queue item

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					transaction,
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

	}

	@Override
	public
	boolean chatMessageDeliverViaSms (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageRec chatMessage,
			@NonNull String text) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessageDeliverViaSms");

		) {

			ChatUserRec fromUser =
				chatMessage.getFromUser ();

			ChatUserRec toUser =
				chatMessage.getToUser ();

			ChatRec chat =
				toUser.getChat ();

			List <String> stringParts;

			try {

				stringParts =
					MessageSplitter.split (
						text,
						messageFromTemplates (
							fromUser));

			} catch (IllegalArgumentException exception) {

				transaction.errorFormatException (
					exception,
					"MessageSplitter.split threw exception");

				exceptionLogger.logSimple (
					transaction,
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
							transaction,
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
						transaction,
						part));

			}

			Long threadId =
				chatSendLogic.sendMessageMagic (
					transaction,
					toUser,
					optionalFromNullable (
						chatMessage.getThreadId ()),
					textParts,
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"chat"),
					serviceHelper.findByCodeRequired (
						transaction,
						chat,
						serviceCode),
					fromUser.getId (),
					optionalFromNullable (
						chatMessage.getSender ()));

			chatMessage

				.setThreadId (
					threadId)

				.setMethod (
					ChatMessageMethod.sms);

			return true;

		}

	}

	@Override
	public
	String chatMessagePrependWarning (
			@NonNull Transaction parentTransaction,
			@NonNull ChatMessageRec chatMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatMessagePrependWarning");

		) {

			String text =
				chatMessage.getEditedText ().getText ();

			// prepend a warning if appropriate

			if (chatMessage.getMonitorWarning ()) {

				ChatUserRec chatUser =
					chatMessage.getToUser ();

				ChatHelpTemplateRec template =
					chatTemplateLogic.findChatHelpTemplate (
						transaction,
						chatUser,
						"system",
						warningByOperatorLabel.get (
							chatUser.getOperatorLabel ()));

				text =
					template.getText () + text;

			}

			return text;

		}

	}

	static
	Map <ChatUserOperatorLabel, String> warningByOperatorLabel =
		ImmutableMap.<ChatUserOperatorLabel, String> builder ()

		.put (
			ChatUserOperatorLabel.operator,
			"operator_message_warning")

		.put (
			ChatUserOperatorLabel.monitor,
			"monitor_message_warning")

		.build ();

	@Override
	public
	void chatUserRejectionCountInc (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatUserRejectionCountInc");

		) {

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
				transaction,
				chatUser,
				optionalOf (
					message.getThreadId ()),
				"adult_hint_in",
				chatScheme.getRbFreeRouter (),
				chatScheme.getAdultAdsScheme ().getRbNumber (),
				Collections.emptySet (),
				optionalAbsent (),
				"system",
				TemplateMissing.error,
				emptyMap ());

		}

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
