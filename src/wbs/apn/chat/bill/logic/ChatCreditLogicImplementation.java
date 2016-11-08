package wbs.apn.chat.bill.logic;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.notEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.Collections;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

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
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

@Log4j
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
			ChatUserRec toChatUser,
			int receivedMessageCount) {

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
			toChatUser,
			amount);

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
			ChatUserRec chatUser,
			int userMessageCount,
			int monitorMessageCount,
			int textProfileCount,
			int imageProfileCount,
			int videoProfileCount) {

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
			chatUser,
			amount);

	}

	@Override
	public
	void chatUserSpendBasic (
			ChatUserRec chatUser,
			int amount) {

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
			ChatUserRec chatUser,
			LocalDate date) {

		ChatUserSpendRec chatUserSpend =
			chatUserSpendHelper.findByDate (
				chatUser,
				date);

		if (chatUserSpend == null) {

			chatUserSpend =
				chatUserSpendHelper.insert (
					chatUserSpendHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setDate (
					date)

			);

		}

		return chatUserSpend;
	}

	@Override
	public
	ChatCreditCheckResult userSpendCreditCheck (
			@NonNull ChatUserRec chatUser,
			@NonNull Boolean userActed,
			@NonNull Optional <Long> threadId) {

		log.debug (
			stringFormat (
				"userSpendCheck (%s, %s, %s)",
				integerToDecimalString (
					chatUser.getId ()),
				booleanToYesNo (
					userActed),
				threadId.isPresent ()
					? threadId.get ().toString ()
					: "null"));

		// if user acted then clear block and send pending bill

		if (userActed) {

			chatUser

				.setBlockAll (
					false);

			userBill (
				chatUser,
				new BillCheckOptions ()
					.retry (true));

		}

		// check their credit

		ChatCreditCheckResult creditCheckResult =
			userCreditCheck (
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
				chatUser,
				threadId);

		}

		// if credit bad, log them off and cancel ad

		if (creditCheckResult.failed ()) {

			chatUserLogic.logoff (
				chatUser,
				threadId == null);

			chatUser

				.setNextAd (
					null);

		}

		// return

		return creditCheckResult;

	}

	@Override
	public
	ChatCreditCheckResult userCreditCheck (
			@NonNull ChatUserRec chatUser) {

		log.debug (
			stringFormat (
				"userCreditOk (%s)",
				integerToDecimalString (
					chatUser.getId ())));

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
				chatUser);

		case prePay:
			return userCreditCheckPrepay (
				chatUser);

		case barred:
			return ChatCreditCheckResult.failedBarred;

		case free:
			return ChatCreditCheckResult.passedFree;

		}

		throw new RuntimeException ();

	}

	@Override
	public
	ChatCreditCheckResult userCreditCheckStrict (
			ChatUserRec chatUser) {

		// sanity check

		if (chatUser.getCreditMode () != ChatUserCreditMode.billedMessages)
			throw new IllegalArgumentException ();

		// check network

		if (chatUser.getNumber ().getNetwork ().getId () == 0)
			return ChatCreditCheckResult.failedNoNetwork;

		Optional<ChatNetworkRec> chatNetworkOptional =
			chatNetworkHelper.forUser (
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

	ChatCreditCheckResult userCreditCheckPrepay (
			ChatUserRec chatUser) {

		// sanity check

		if (chatUser.getCreditMode () != ChatUserCreditMode.prePay)
			throw new IllegalArgumentException ();

		// check network

		if (chatUser.getNumber ().getNetwork ().getId () == 0)
			return ChatCreditCheckResult.failedNoNetwork;

		Optional<ChatNetworkRec> chatNetworkOptional =
			chatNetworkHelper.forUser (
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

	@Override
	public
	Optional <String> userBillCheck (
			@NonNull ChatUserRec chatUser,
			@NonNull BillCheckOptions options) {

		Transaction transaction =
			database.currentTransaction ();

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// check user has number

		if (chatUser.getNumber () == null) {

			return optionalOf (
				stringFormat (
					"Unable to bill chat user %s with no number",
					objectManager.objectPathMini (
						chatUser)));

		}

		// check number is successful

		if (

			! chatNumberReportLogic.isNumberReportSuccessful (
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

		if (userBillLimitApplies (chatUser)) {

			return optionalOf (
				stringFormat (
					"Ignoring credit request for %s due to daily ",
					objectManager.objectPathMini (
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

	@Override
	public
	void userBill (
			@NonNull ChatUserRec chatUser,
			@NonNull BillCheckOptions options) {

		Optional <String> reasonOptional =
			userBillCheck (
				chatUser,
				options);

		if (
			optionalIsPresent (
				reasonOptional)
		) {

			log.warn (
				reasonOptional.get ());

		} else {

			userBillReal (
				chatUser,
				true);

		}

	}

	@Override
	public
	void userBillReal (
			ChatUserRec chatUser,
			boolean updateRevoked) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		RouteRec route =
			chatScheme.getRbBillRoute ();

		log.debug (
			stringFormat (
				"Doing credit for user %s",
				integerToDecimalString (
					chatUser.getId ())));

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
				? Optional.of (
					chatUser.getCreditMode () == ChatUserCreditMode.billedMessages
						? "chat_bill_strict"
						: "chat_bill")
				: Optional.<String>absent ();

		// lookup the template

		ChatHelpTemplateRec chatHelpTemplate =
			chatTemplateLogic.findChatHelpTemplate (
				chatUser,
				"system",
				"billed_message");

		// substitute the params

		String originalText =
			chatHelpTemplate.getText ();

		Map<String,String> allParams =
			chatSendLogic.addDefaultParams (
				chatUser,
				Collections.<String,String>emptyMap ());

		String finalText =
			MapStringSubstituter.substitute (
				originalText,
				allParams);

		TextRec text =
			textHelper.findOrCreate (
				finalText);

		// and send it

		ServiceRec billService =
			serviceHelper.findByCodeRequired (
				chat,
				"bill");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
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
				deliveryTypeCode)

			.ref (
				chatUser.getId ())

			.send ();

		// Bill message has been sent, now tidy up for 30 pound limit

		chatUser

			.setCreditDailyAmount (
				+ chatUser.getCreditDailyAmount ()
				+ route.getOutCharge ());

		chatUser

			.setLastBillSent (
				transaction.now ());

		log.info (
			stringFormat (
				"Billed message sent to chat user %s %s",
				integerToDecimalString (
					chatUser.getId ()),
				userCreditDebug (
					chatUser)));

	}

	@Override
	public
	long userBillLimitAmount (
			ChatUserRec chatUser) {

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

	@Override
	public
	boolean userBillLimitApplies (
			ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

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
				chatUser);

		long newDailyBilledAmount =
			+ chatUser.getCreditDailyAmount ()
			+ routeCharge;

		return newDailyBilledAmount > limit;

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
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId) {

		Transaction transaction =
			database.currentTransaction ();

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
					chatUser);

			if (! chatNetworkOptional.isPresent ()) {

				log.warn (
					stringFormat (
						"Not sending credit hint to %s ",
						integerToDecimalString (
							chatUser.getId ()),
						"because no network settings found"));

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
						chatUser,
						threadId,
						"credit_hint_network",
						TemplateMissing.error,
						Collections.<String,String>emptyMap ());

				} else {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						threadId,
						"credit_hint_prepay",
						TemplateMissing.error,
						Collections.<String,String>emptyMap ());

				}

			} else if (
				enumEqualSafe (
					chatUser.getCreditMode (),
					ChatUserCreditMode.billedMessages)
			) {

				if (! chatNetwork.getAllowReverseBill ()) {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						threadId,
						"credit_hint_network",
						TemplateMissing.error,
						Collections.<String,String>emptyMap ());

				} else if (
					userBillLimitApplies (
						chatUser)
				) {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						threadId,
						"credit_hint_daily",
						TemplateMissing.error,
						ImmutableMap.<String,String>builder ()

							.put (
								"limit",
								Long.toString (
									userBillLimitAmount (chatUser) / 100))

							.build ());

				} else {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						threadId,
						"credit_hint",
						TemplateMissing.error,
						Collections.<String,String>emptyMap ());

				}

			} else {

				throw new RuntimeException ();

			}

			chatUser

				.setLastCreditHint (
					transaction.now ());

		}

	}

	@Override
	public
	void doRebill () {

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
			ChatUserRec chatUser) {

		Transaction transaction =
			database.currentTransaction ();

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

	@Override
	public
	String userCreditDebug (
			ChatUserRec chatUser) {

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
