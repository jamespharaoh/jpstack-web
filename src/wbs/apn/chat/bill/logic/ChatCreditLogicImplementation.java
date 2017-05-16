package wbs.apn.chat.bill.logic;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.notEqualSafe;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.misc.MapStringSubstituter;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.bill.model.ChatUserCreditLimitLogObjectHelper;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.bill.model.ChatUserSpendObjectHelper;
import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

@SingletonComponent ("chatCreditLogic")
public
class ChatCreditLogicImplementation
	implements ChatCreditLogic {

	// singleton dependencies

	@SingletonDependency
	ChatNumberReportLogic chatNumberReportLogic;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatHelpTemplateLogic chatTemplateLogic;

	@SingletonDependency
	ChatNetworkObjectHelper chatNetworkHelper;

	@SingletonDependency
	ChatUserCreditLimitLogObjectHelper chatUserCreditLimitLogHelper;

	@SingletonDependency
	ChatUserSpendObjectHelper chatUserSpendHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// implementation

	@Override
	public
	void userReceiveSpend (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec toChatUser,
			@NonNull Long receivedMessageCount) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userReceiveSpend");

		) {

			if (toChatUser.getType () == ChatUserType.monitor)
				return;

			ChatSchemeRec chatScheme =
				toChatUser.getChatScheme ();

			ChatSchemeChargesRec chatSchemeCharges =
				chatScheme.getCharges ();

			// iphone users are never charged

			boolean free =
				toChatUser.getDeliveryMethod () == ChatMessageMethod.iphone;

			DateTimeZone timeZone =
				DateTimeZone.forID (
					chatScheme.getTimezone ());

			LocalDate today =
				LocalDate.now (
					timeZone);

			ChatUserSpendRec toChatUserSpend =
				findOrCreateChatUserSpend (
					transaction,
					toChatUser,
					today);

			toChatUserSpend.setReceivedMessageCount (
				toChatUserSpend.getReceivedMessageCount ()
					+ receivedMessageCount);

			if (! free) {

				toChatUserSpend.setReceivedMessageCharge (
					toChatUserSpend.getReceivedMessageCharge ()
						+ receivedMessageCount
							* chatSchemeCharges.getChargeChatReceive ());
			}

			toChatUser.setReceivedMessageCount (
				toChatUser.getReceivedMessageCount ()
					+ receivedMessageCount);

			if (! free) {

				toChatUser.setReceivedMessageCharge (
					toChatUser.getReceivedMessageCharge ()
						+ receivedMessageCount
							* chatSchemeCharges.getChargeChatReceive ());

			}

			// charge

			int amount = 0;

			if (! free) {

				amount +=
					chatSchemeCharges.getChargeChatReceive ()
						* receivedMessageCount;

			}

			// update the chat user

			chatUserSpendBasic (
				transaction,
				toChatUser,
				amount);

		}

	}

	/**
	 * Bills a user the specified amount. This increases their valueSinceXxx
	 * counters and reduces their credit. If they have free usage it increases
	 * their creditAdded by the same amount and the overall credit is
	 * unaffected.
	 */
	@Override
	public
	void userSpend (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			int userMessageCount,
			int monitorMessageCount,
			int textProfileCount,
			int imageProfileCount,
			int videoProfileCount) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userSpend");

		) {

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			ChatSchemeChargesRec chatSchemeCharges =
				chatScheme.getCharges ();

			// iphone users are never charged

			boolean free =
				chatUser.getDeliveryMethod () == ChatMessageMethod.iphone;

			// update the chat user spend

			DateTimeZone timeZone =
				DateTimeZone.forID (
					chatScheme.getTimezone ());

			LocalDate today =
				LocalDate.now (
					timeZone);

			ChatUserSpendRec chatUserSpend =
				findOrCreateChatUserSpend (
					transaction,
					chatUser,
					today);

			chatUserSpend.setUserMessageCount (
				chatUserSpend.getUserMessageCount ()
					+ userMessageCount);

			chatUserSpend.setMonitorMessageCount (
				chatUserSpend.getMonitorMessageCount ()
					+ monitorMessageCount);

			chatUserSpend.setTextProfileCount (
				chatUserSpend.getTextProfileCount ()
					+ textProfileCount);

			chatUserSpend.setImageProfileCount (
				chatUserSpend.getImageProfileCount ()
					+ imageProfileCount);

			chatUserSpend.setVideoProfileCount (
				chatUserSpend.getVideoProfileCount ()
					+ videoProfileCount);

			if (! free) {

				chatUserSpend.setUserMessageCharge (
					chatUserSpend.getUserMessageCharge ()
						+ userMessageCount
							* chatSchemeCharges.getChargeChatSend ());

				chatUserSpend.setMonitorMessageCharge (
					chatUserSpend.getMonitorMessageCharge ()
						+ monitorMessageCount
							* chatSchemeCharges.getChargeChatSend ());

				chatUserSpend.setTextProfileCharge (
					chatUserSpend.getTextProfileCharge ()
						+ textProfileCount
							* chatSchemeCharges.getChargeChatInfo ());

				chatUserSpend.setImageProfileCharge (
					chatUserSpend.getImageProfileCharge ()
						+ imageProfileCount
							* chatSchemeCharges.getChargeChatPic ()
							/ chatSchemeCharges.getChargeChatPicDiv ());

				chatUserSpend.setVideoProfileCharge (
					chatUserSpend.getVideoProfileCharge ()
						+ videoProfileCount
							* chatSchemeCharges.getChargeChatVideo ()
							/ chatSchemeCharges.getChargeChatVideoDiv ());

			}

			// update the chat user

			chatUser.setUserMessageCount (
				chatUser.getUserMessageCount ()
					+ userMessageCount);

			chatUser.setMonitorMessageCount (
				chatUser.getMonitorMessageCount ()
					+ monitorMessageCount);

			chatUser.setTextProfileCount (
				chatUser.getTextProfileCount ()
					+ textProfileCount);

			chatUser.setImageProfileCount (
				chatUser.getImageProfileCount ()
					+ imageProfileCount);

			chatUser.setVideoProfileCount (
				chatUser.getVideoProfileCount ()
					+ videoProfileCount);

			if (! free) {

				chatUser.setUserMessageCharge (
					chatUser.getUserMessageCharge ()
						+ userMessageCount
							* chatSchemeCharges.getChargeChatSend ());

				chatUser.setMonitorMessageCharge (
					chatUser.getMonitorMessageCharge ()
						+ monitorMessageCount
							* chatSchemeCharges.getChargeChatSend ());

				chatUser.setTextProfileCharge (
					chatUser.getTextProfileCharge ()
						+ textProfileCount
							* chatSchemeCharges.getChargeChatInfo ());

				chatUser.setImageProfileCharge (
					chatUser.getImageProfileCharge ()
						+ imageProfileCount
							* chatSchemeCharges.getChargeChatPic ()
							/ chatSchemeCharges.getChargeChatPicDiv ());

				chatUser.setVideoProfileCharge (
					chatUser.getVideoProfileCharge ()
						+ videoProfileCount
							* chatSchemeCharges.getChargeChatVideo ()
							/ chatSchemeCharges.getChargeChatVideoDiv ());
			}

			// work out the total

			int amount = 0;

			if (! free) {

				amount +=
					chatSchemeCharges.getChargeChatSend ()
						* (userMessageCount + monitorMessageCount);

				amount +=
					chatSchemeCharges.getChargeChatInfo ()
						* textProfileCount;

				amount +=
					chatSchemeCharges.getChargeChatPic ()
						* imageProfileCount
						/ chatSchemeCharges.getChargeChatPicDiv ();

				amount +=
					chatSchemeCharges.getChargeChatVideo ()
						* videoProfileCount
						/ chatSchemeCharges.getChargeChatVideoDiv ();
			}

			// update the chat user

			chatUserSpendBasic (
				transaction,
				chatUser,
				amount);

		}

	}

	@Override
	public
	void chatUserSpendBasic (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			int amount) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatUserSpendBasic");

		) {

			chatUser

				.setValueSinceWarning (
					chatUser.getValueSinceWarning () + amount)

				.setValueSinceEver (
					chatUser.getValueSinceEver () + amount);

			if (chatUser.getCreditMode () == ChatUserCreditMode.free) {

				chatUser

					.setCreditAdded (
						chatUser.getCreditAdded () + amount);

			} else {

				chatUser

					.setCredit (
						chatUser.getCredit () - amount);

			}

		}

	}

	/**
	 * @param chatUser
	 *            the chat user to associate with
	 * @param date
	 *            the date to associate with
	 * @return the new/existing chat user spend record
	 */
	@Override
	public
	ChatUserSpendRec findOrCreateChatUserSpend (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull LocalDate date) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreateChatUserSpend");

		) {

			ChatUserSpendRec chatUserSpend =
				chatUserSpendHelper.findByDate (
					transaction,
					chatUser,
					date);

			if (chatUserSpend == null) {

				chatUserSpend =
					chatUserSpendHelper.insert (
						transaction,
						chatUserSpendHelper.createInstance ()

					.setChatUser (
						chatUser)

					.setDate (
						date)

				);

			}

			return chatUserSpend;

		}

	}

	@Override
	public
	ChatCreditCheckResult userSpendCreditCheck (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Boolean userActed,
			@NonNull Optional <Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userSpendCreditCheck");

		) {

			transaction.debugFormat (
				"userSpendCheck (%s, %s, %s)",
				integerToDecimalString (
					chatUser.getId ()),
				booleanToYesNo (
					userActed),
				threadId.isPresent ()
					? threadId.get ().toString ()
					: "null");

			// if user acted then clear block and send pending bill

			if (userActed) {

				chatUser

					.setBlockAll (
						false);

				userBill (
					transaction,
					chatUser,
					new BillCheckOptions ()
						.retry (true));

			}

			// check their credit

			ChatCreditCheckResult creditCheckResult =
				userCreditCheck (
					transaction,
					chatUser);

			// if credit ok, clear credit hint

			if (creditCheckResult.passed ()) {

				chatUser

					.setLastCreditHint (
						null);

			}

			// if credit bad, and acted, send credit hint

			if (
				creditCheckResult.failed ()
				&& userActed
				&& chatUser.getFirstJoin () != null
			) {

				userCreditHint (
					transaction,
					chatUser,
					threadId);

			}

			// if credit bad, log them off and cancel ad

			if (creditCheckResult.failed ()) {

				chatUserLogic.logoff (
					transaction,
					chatUser,
					isNull (
						threadId));

				chatUser

					.setNextAd (
						null);

			}

			// return

			return creditCheckResult;

		}

	}

	@Override
	public
	ChatCreditCheckResult userCreditCheck (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userCreditCheck");

		) {

			transaction.debugFormat (
				"userCreditOk (%s)",
				integerToDecimalString (
					chatUser.getId ()));

			// monitors always pass

			if (chatUser.getType () == ChatUserType.monitor)
				return ChatCreditCheckResult.monitor;

			// deleted users always fail

			if (chatUser.getNumber () == null)
				return ChatCreditCheckResult.failedNoNumber;

			// blocked users fail unless the caller disables this check

			if (chatUser.getBlockAll ())
				return ChatCreditCheckResult.failedBlocked;

			// barred users always fail

			if (chatUser.getBarred ())
				return ChatCreditCheckResult.failedBarred;

			// further checks based on credit mode

			switch (chatUser.getCreditMode ()) {

			case billedMessages:

				return userCreditCheckStrict (
					transaction,
					chatUser);

			case prePay:

				return userCreditCheckPrepay (
					transaction,
					chatUser);

			case barred:

				return ChatCreditCheckResult.failedBarred;

			case free:

				return ChatCreditCheckResult.passedFree;

			}

			throw new RuntimeException ();

		}

	}

	@Override
	public
	ChatCreditCheckResult userCreditCheckStrict (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userCreditCheckStrict");

		) {

			// sanity check

			if (chatUser.getCreditMode () != ChatUserCreditMode.billedMessages)
				throw new IllegalArgumentException ();

			// check network

			if (chatUser.getNumber ().getNetwork ().getId () == 0)
				return ChatCreditCheckResult.failedNoNetwork;

			Optional<ChatNetworkRec> chatNetworkOptional =
				chatNetworkHelper.forUser (
					transaction,
					chatUser);

			if (! chatNetworkOptional.isPresent ())
				return ChatCreditCheckResult.failedInvalidNetwork;

			ChatNetworkRec chatNetwork =
				chatNetworkOptional.get ();

			if (! chatNetwork.getAllowReverseBill ())
				return ChatCreditCheckResult.failedReverseBillDisabledForNetwork;

			// can't check credit without a scheme

			if (chatUser.getChatScheme () == null)
				return ChatCreditCheckResult.failedNoChatScheme;

			// check credit

			long effectiveCredit =
				+ chatUser.getCredit ()
				- chatUser.getCreditPendingStrict ();

			ChatSchemeChargesRec charges =
				chatUser.getChatScheme ().getCharges ();

			boolean passed =
				effectiveCredit >= - charges.getCreditLimit ()
				&& effectiveCredit >= - chatUser.getCreditLimit ();

			return passed
				? ChatCreditCheckResult.passedStrict
				: ChatCreditCheckResult.failedStrict;

		}

	}

	ChatCreditCheckResult userCreditCheckPrepay (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userCreditCheckPrepay");

		) {

			// sanity check

			if (chatUser.getCreditMode () != ChatUserCreditMode.prePay)
				throw new IllegalArgumentException ();

			// check network

			if (chatUser.getNumber ().getNetwork ().getId () == 0)
				return ChatCreditCheckResult.failedNoNetwork;

			Optional<ChatNetworkRec> chatNetworkOptional =
				chatNetworkHelper.forUser (
					transaction,
					chatUser);

			if (! chatNetworkOptional.isPresent ())
				return ChatCreditCheckResult.failedInvalidNetwork;

			ChatNetworkRec chatNetwork =
				chatNetworkOptional.get ();

			if (! chatNetwork.getAllowPrePay ())
				return ChatCreditCheckResult.failedPrepayDisabledForNetwork;

			// check prepay credit

			boolean passed =
				chatUser.getCredit () >= 0;

			return passed
				? ChatCreditCheckResult.passedPrepay
				: ChatCreditCheckResult.failedPrepay;

		}

	}

	@Override
	public
	Optional <String> userBillCheck (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull BillCheckOptions options) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userBillCheck");

		) {

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			// check user has number

			if (chatUser.getNumber () == null) {

				return optionalOf (
					stringFormat (
						"Unable to bill chat user %s with no number",
						objectManager.objectPathMini (
							transaction,
							chatUser)));

			}

			// check number is successful

			if (

				! chatNumberReportLogic.isNumberReportSuccessful (
					transaction,
					chatUser.getNumber ())

				&& ! options.includeFailed ()

			) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"due to repeated billed message failure"));

			}

			// check number has not had too many permanent failures

			if (
				chatNumberReportLogic
					.isNumberReportPastPermanentDeliveryConstraint (
						transaction,
						chatUser.getNumber ())
			) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s because permanent failure",
						integerToDecimalString (
							chatUser.getId ())));

			}

			// check block all

			if (

				chatUser.getBlockAll ()

				&& ! (
					chatUser.getChat ().getBillBlockedUsers ()
					|| options.includeBlocked ()
				)

			) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s because block all",
						integerToDecimalString (
							chatUser.getId ())));

			}

			// check they are on strict billing

			if (
				enumNotEqualSafe (
					chatUser.getCreditMode (),
					ChatUserCreditMode.billedMessages)
			) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"as their credit mode is %s",
						chatUser.getCreditMode ().toString ()));

			}

			// check they are still below 0

			if (chatUser.getCredit () >= 0) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s as their credit is %s",
						integerToDecimalString (
							chatUser.getId ()),
						currencyLogic.formatText (
							chatUser.getChat ().getCurrency (),
							chatUser.getCredit ())));

			}

			// check we aren't retrying revoked credit if we aren't supposed to

			boolean revoked =
				chatUser.getCredit () + chatUser.getCreditRevoked () >= 0;

			if (revoked && ! options.retry ()) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"as credit is revoked and retry is disabled"));

			}

			// apply daily bill limit

			if (
				userBillLimitApplies (
					transaction,
					chatUser)
			) {

				return optionalOf (
					stringFormat (
						"Ignoring credit request for %s due to daily ",
						objectManager.objectPathMini (
							transaction,
							chatUser),
						"limit"));

			}

			// work out start of day

			DateTimeZone timeZone =
				DateTimeZone.forID (
					chatScheme.getTimezone ());

			Instant startOfToday =
				transaction.now ()
					.toDateTime (timeZone)
					.withTimeAtStartOfDay ()
					.toInstant ();

			// check user hasnt been sent a bill today that wasn't successful

			if (

				isNotNull (
					chatUser.getLastBillSent ())

				&& laterThan (
					chatUser.getLastBillSent (),
					startOfToday)

			) {

				return optionalOf (
					stringFormat (
						"Rejecting bill for user %s because last sent %s",
						integerToDecimalString (
							chatUser.getId ()),
						timeFormatter.timestampSecondStringIso (
							chatUser.getLastBillSent ())));

			}

			// return success

			return optionalAbsent ();

		}

	}

	@Override
	public
	void userBill (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull BillCheckOptions options) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userBill");

		) {

			Optional <String> reasonOptional =
				userBillCheck (
					transaction,
					chatUser,
					options);

			if (
				optionalIsPresent (
					reasonOptional)
			) {

				transaction.debugFormat (
					"%s",
					reasonOptional.get ());

			} else {

				userBillReal (
					transaction,
					chatUser,
					true);

			}

		}

	}

	@Override
	public
	void userBillReal (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			boolean updateRevoked) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userBillReal");

		) {

			ChatRec chat =
				chatUser.getChat ();

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			RouteRec route =
				chatScheme.getRbBillRoute ();

			transaction.debugFormat (
				"Doing credit for user %s",
				integerToDecimalString (
					chatUser.getId ()));

			// sanity check on the route

			if (! route.getDeliveryReports ()) {

				throw new RuntimeException (
					stringFormat (
						"Can't use route %s as billed for chat scheme %s because ",
						integerToDecimalString (
							route.getId ()),
						integerToDecimalString (
							chatScheme.getId ()),
						"delivery reports are disabled"));

			}

			if (route.getExpirySecs () == null) {

				throw new RuntimeException (
					stringFormat (
						"Can't use route %s as billed for chat scheme %s because ",
						integerToDecimalString (
							route.getId ()),
						integerToDecimalString (
							chatScheme.getId ()),
						"no expiry time is configured"));

			}

			// update user credit

			chatUser

				.setCredit (
					chatUser.getCredit () + route.getOutCharge ());

			if (route.getDeliveryReports ()) {

				if (chatUser.getCreditMode () == ChatUserCreditMode.billedMessages) {

					chatUser

						.setCreditPendingStrict (
							+ chatUser.getCreditPendingStrict ()
							+ route.getOutCharge ());

				} else {

					chatUser

						.setCreditPending (
							+ chatUser.getCreditPending ()
							+ route.getOutCharge ());

				}

			} else {

				chatUser

					.setCreditSent (
						+ chatUser.getCreditSent ()
						+ route.getOutCharge ());

			}

			// update revoked if appropriate

			boolean revoked =
				updateRevoked && (
					+ chatUser.getCredit ()
					- route.getOutCharge ()
					+ chatUser.getCreditRevoked ()
				) >= 0;

			if (revoked) {

				chatUser

					.setCreditRevoked (
						+ chatUser.getCreditRevoked ()
						- route.getOutCharge ())

					.setCreditRetried (
						+ chatUser.getCreditRetried ()
						+ route.getOutCharge ());

				if (chatUser.getCreditRevoked () < 0) {

					chatUser

						.setCreditRetried (
							+ chatUser.getCreditRetried ()
							+ chatUser.getCreditRevoked ())

						.setCreditRevoked (
							+ chatUser.getCreditRevoked ()
							- chatUser.getCreditRevoked ());

				}

			}

			// get delivery notice type

			Optional<String> deliveryTypeCode =
				route.getDeliveryReports ()
					? optionalOfFormat (
						chatUser.getCreditMode () == ChatUserCreditMode.billedMessages
							? "chat_bill_strict"
							: "chat_bill")
					: optionalAbsent ();

			// lookup the template

			ChatHelpTemplateRec chatHelpTemplate =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					chatUser,
					"system",
					"billed_message");

			// substitute the params

			String originalText =
				chatHelpTemplate.getText ();

			Map <String, String> allParams =
				chatSendLogic.addDefaultParams (
					transaction,
					chatUser,
					emptyMap ());

			String finalText =
				MapStringSubstituter.substitute (
					originalText,
					allParams);

			TextRec text =
				textHelper.findOrCreate (
					transaction,
					finalText);

			// and send it

			ServiceRec billService =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"bill");

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					chatUser);

			messageSender.get ()

				.number (
					chatUser.getNumber ())

				.messageText (
					text)

				.numFrom (
					chatScheme.getRbNumber ())

				.route (
					route)

				.service (
					billService)

				.affiliate (
					affiliate)

				.deliveryTypeCode (
					transaction,
					deliveryTypeCode)

				.ref (
					chatUser.getId ())

				.send (
					transaction);

			// Bill message has been sent, now tidy up for 30 pound limit

			chatUser

				.setCreditDailyAmount (
					+ chatUser.getCreditDailyAmount ()
					+ route.getOutCharge ());

			chatUser

				.setLastBillSent (
					transaction.now ());

			transaction.noticeFormat (
				"Billed message sent to chat user %s %s",
				integerToDecimalString (
					chatUser.getId ()),
				userCreditDebug (
					transaction,
					chatUser));

		}

	}

	@Override
	public
	long userBillLimitAmount (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userBillLimitAmount");

		) {

			boolean onAdultService =
				stringEqualSafe (
					chatUser.getChat ().getCode (),
					"adult");

			// TODO this should be in configuration

			boolean onLowLimitNetwork =
				stringInSafe (
					chatUser.getNumber ().getNetwork ().getCode (),
					"uk_o2",
					"uk_orange",
					"uk_vodafone");

			ChatSchemeChargesRec charges =
				chatUser.getChatScheme ().getCharges ();

			return onAdultService || onLowLimitNetwork
				? charges.getBillLimitLow ()
				: charges.getBillLimitHigh ();

		}

	}

	@Override
	public
	boolean userBillLimitApplies (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userBillLimitApplies");

		) {

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			if (chatUser.getChatScheme () == null)
				return false;

			ChatSchemeChargesRec charges =
				chatScheme.getCharges ();

			if (! charges.getBillLimitEnabled ())
				return false;

			// work out what day we're on

			DateTimeZone timeZone =
				DateTimeZone.forID (
					chatScheme.getTimezone ());

			LocalDate today =
				transaction.now ()
					.toDateTime (timeZone)
					.toLocalDate ();

			// reset the limit on a new day

			if (

				chatUser.getCreditDailyDate () == null

				|| notEqualSafe (
					chatUser.getCreditDailyDate (),
					today)

			) {

				chatUser

					.setCreditDailyDate (
						today)

					.setCreditDailyAmount (
						0l);

			}

			// apply the limit

			long routeCharge =
				chatScheme.getRbBillRoute ().getOutCharge ();

			long limit =
				userBillLimitAmount (
					transaction,
					chatUser);

			long newDailyBilledAmount =
				+ chatUser.getCreditDailyAmount ()
				+ routeCharge;

			return newDailyBilledAmount > limit;

		}

	}

	/**
	 * Sends a credit hint to a chat user, unless they are barred or blocked or
	 * have had one very recently.
	 *
	 * @param chatUser
	 *            the chat user to send the hint to
	 * @param threadId
	 *            the threadId to associate the message with, or null
	 */
	@Override
	public
	void userCreditHint (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userCreditHint");

		) {

			if (chatUser.getBarred ())
				return;

			if (chatUser.getBlockAll ())
				return;

			if (

				isNull (
					chatUser.getLastCreditHint ())

				|| earlierThan (
					chatUser.getLastCreditHint (),
					transaction.now ().minus (
						1000 * 60 * 60 * 24))

			) {

				// send message as appropriate

				Optional <ChatNetworkRec> chatNetworkOptional =
					chatNetworkHelper.forUser (
						transaction,
						chatUser);

				if (! chatNetworkOptional.isPresent ()) {

					transaction.debugFormat (
						"Not sending credit hint to %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"because no network settings found");

					return;

				}

				ChatNetworkRec chatNetwork =
					chatNetworkOptional.get ();

				if (
					enumEqualSafe (
						chatUser.getCreditMode (),
						ChatUserCreditMode.prePay)
				) {

					if (! chatNetwork.getAllowPrePay ()) {

						chatSendLogic.sendSystemRbFree (
							transaction,
							chatUser,
							threadId,
							"credit_hint_network",
							TemplateMissing.error,
							emptyMap ());

					} else {

						chatSendLogic.sendSystemRbFree (
							transaction,
							chatUser,
							threadId,
							"credit_hint_prepay",
							TemplateMissing.error,
							emptyMap ());

					}

				} else if (
					enumEqualSafe (
						chatUser.getCreditMode (),
						ChatUserCreditMode.billedMessages)
				) {

					if (! chatNetwork.getAllowReverseBill ()) {

						chatSendLogic.sendSystemRbFree (
							transaction,
							chatUser,
							threadId,
							"credit_hint_network",
							TemplateMissing.error,
							emptyMap ());

					} else if (
						userBillLimitApplies (
							transaction,
							chatUser)
					) {

						chatSendLogic.sendSystemRbFree (
							transaction,
							chatUser,
							threadId,
							"credit_hint_daily",
							TemplateMissing.error,
							ImmutableMap.of (
								"limit",
								Long.toString (
									userBillLimitAmount (
										transaction,
										chatUser
									) / 100)));

					} else {

						chatSendLogic.sendSystemRbFree (
							transaction,
							chatUser,
							threadId,
							"credit_hint",
							TemplateMissing.error,
							emptyMap ());

					}

				} else {

					throw new RuntimeException ();

				}

				chatUser

					.setLastCreditHint (
						transaction.now ());

			}

		}

	}

	@Override
	public
	void doRebill (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	/**
	 * Checks if a user has a credit limit less than their successful delivered
	 * count rounded down to the nearest thousand (ten pounds) plus one thousand
	 * (ten pounds), if so it raises their credit limit to that amount and logs
	 * the event.
	 *
	 * @param chatUser
	 *            The user to check
	 */
	@Override
	public
	void creditLimitUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"creditLimitUpdate");

		) {

			// work out the minimum we are aiming for

			long targetLimit =
				sum (
					+ chatUser.getCreditSuccess (),
					- (chatUser.getCreditSuccess () % 1000) + 1000);

			// if it's already above that do nothing

			if (chatUser.getCreditLimit () >= targetLimit)
				return;

			// log it

			chatUserCreditLimitLogHelper.insert (
				transaction,
				chatUserCreditLimitLogHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setOldCreditLimit (
					chatUser.getCreditLimit ())

				.setNewCreditLimit (
					targetLimit)

			);

			// update the user's credit limit

			chatUser

				.setCreditLimit (
					targetLimit);

		}

	}

	@Override
	public
	String userCreditDebug (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"userCreditDebug");

		) {

			return stringFormat (

				"mode:%s ",
				chatUser.getCreditMode ().toString (),

				"credit:%s ",
				currencyLogic.formatText (
					chatUser.getChat ().getCurrency (),
					chatUser.getCredit ()),

				"pending:%s ",
				currencyLogic.formatSimple (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditPending ()),

				"strict:%s ",
				currencyLogic.formatSimple (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditPendingStrict ()),

				"sent:%s ",
				currencyLogic.formatText (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditSent ()),

				"revoked:%s ",
				currencyLogic.formatText (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditRevoked ()),

				"retried:%s",
				currencyLogic.formatText (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditRetried ()));

		}

	}

}
