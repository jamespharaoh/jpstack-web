package wbs.clients.apn.chat.bill.logic;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.earlierThan;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.laterThan;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.sum;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatNetworkRec;
import wbs.clients.apn.chat.bill.model.ChatUserCreditLimitLogObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatUserCreditLimitLogRec;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.bill.model.ChatUserSpendObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatUserSpendRec;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.misc.MapStringSubstituter;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("chatCreditLogic")
public
class ChatCreditLogicImplementation
	implements ChatCreditLogic {

	// dependencies

	@Inject
	ChatNumberReportLogic chatNumberReportLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatHelpTemplateLogic chatTemplateLogic;

	@Inject
	ChatNetworkObjectHelper chatNetworkHelper;

	@Inject
	ChatUserCreditLimitLogObjectHelper chatUserCreditLimitLogHelper;

	@Inject
	ChatUserSpendObjectHelper chatUserSpendHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

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
					new ChatUserSpendRec ()

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
			@NonNull Optional<Integer> threadId) {

		log.debug (
			stringFormat (
				"userSpendCheck (%s, %s, %s)",
				chatUser.getId (),
				userActed
					? "yes"
					: "no",
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
				true);

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
			ChatUserRec chatUser) {

		log.debug (
			stringFormat (
				"userCreditOk (%s)",
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

		case strict:
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

		if (chatUser.getCreditMode () != ChatUserCreditMode.strict)
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

		int effectiveCredit =
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

	/**
	 * Bills the user by sending messages, if appropriate.
	 *
	 * The retry parameter indicates we can retry revoked credit. This is only
	 * set when something has happened which prevents users who are not being
	 * billed from being rebilled continually.
	 *
	 * @param chatUser
	 *            the user to bill if appropriate
	 * @param retry
	 *            if true then revoked credit will be retried also
	 */
	@Override
	public
	void userBill (
			ChatUserRec chatUser,
			boolean retry) {

		Transaction transaction =
			database.currentTransaction ();

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		if (chatUser.getNumber () == null) {

			log.warn (
				stringFormat (
					"Unable to bill chat user %s with no number",
					objectManager.objectPathMini (
						chatUser)));

			return;

		}

		// check number is successful

		if (! chatNumberReportLogic.isNumberReportSuccessful (
				chatUser.getNumber ())) {

			log.debug (
				stringFormat (
					"Ignoring credit request for %s because not successful",
					chatUser.getId ()));

			return;

		}

		// check number has not had too many permanent failures

		if (chatNumberReportLogic.isNumberReportPastPermanentDeliveryConstraint (
				chatUser.getNumber ())) {

			log.debug (
				stringFormat (
					"Ignoring credit request for %s because permanent failure",
					chatUser.getId ()));

			return;

		}

		if (chatUser.getBlockAll ()) {

			log.debug (
				stringFormat (
					"Ignoring credit request for %s because block all",
					chatUser.getId ()));

			return;

		}

		// check they are on strict billing

		if (chatUser.getCreditMode () != ChatUserCreditMode.strict) {

			log.debug (
				stringFormat (
					"Ignoring credit request for %s ",
					chatUser.getId (),
					"as their credit mode is %s",
					chatUser.getCreditMode ()));

			return;

		}

		// check they are still below 0

		if (chatUser.getCredit () >= 0) {

			log.debug (
				stringFormat (
					"Ignoring credit request for %s as their credit is %s",
					chatUser.getId (),
					chatUser.getCredit ()));

			return;

		}

		// check we aren't retrying revoked credit if we aren't supposed to

		boolean revoked =
			chatUser.getCredit () + chatUser.getCreditRevoked () >= 0;

		if (revoked && ! retry)
			return;

		// apply daily bill limit

		if (userBillLimitApplies (chatUser)) {

			log.debug (
				stringFormat (
					"rejecting bill message for chat user %s due to daily ",
					objectManager.objectPathMini (
						chatUser),
					"limit"));

			return;

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
				dateToInstant (
					chatUser.getLastBillSent ()),
				startOfToday)

		) {

			log.info (
				stringFormat (
					"Rejecting bill for user %s because last sent %s",
					chatUser.getId (),
					chatUser.getLastBillSent ()));

			return;

		}

		userBillReal (
			chatUser,
			true);

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
				chatUser.getId ()));

		// sanity check on the route

		if (! route.getDeliveryReports ()) {

			throw new RuntimeException (
				stringFormat (
					"Can't use route %s as billed for chat scheme %s because ",
					route.getId (),
					chatScheme.getId (),
					"delivery reports are disabled"));

		}

		if (route.getExpirySecs () == null) {

			throw new RuntimeException (
				stringFormat (
					"Can't use route %s as billed for chat scheme %s because ",
					route.getId (),
					chatScheme.getId (),
					"no expiry time is configured"));

		}

		// update user credit

		chatUser

			.setCredit (
				chatUser.getCredit () + route.getOutCharge ());

		if (route.getDeliveryReports ()) {

			if (chatUser.getCreditMode () == ChatUserCreditMode.strict) {

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

		String deliveryTypeCode =
			route.getDeliveryReports ()
				? chatUser.getCreditMode () == ChatUserCreditMode.strict
					? "chat_bill_strict"
					: "chat_bill"
				: null;

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
			serviceHelper.findByCode (
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
				instantToDate (
					transaction.now ()));

		log.info (
			stringFormat (
				"Billed message sent to chat user %s %s",
				chatUser.getId (),
				userCreditDebug (chatUser)));

	}

	@Override
	public
	int userBillLimitAmount (
			ChatUserRec chatUser) {

		boolean onAdultService =
			equal (
				chatUser.getChat ().getCode (),
				"adult");

		boolean onLowLimitNetwork =
			in (chatUser.getNumber ().getNetwork ().getCode (),
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

			|| notEqual (
				chatUser.getCreditDailyDate (),
				today)

		) {

			chatUser

				.setCreditDailyDate (
					today)

				.setCreditDailyAmount (
					0);

		}

		// apply the limit

		int routeCharge =
			chatScheme.getRbBillRoute ().getOutCharge ();

		int limit =
			userBillLimitAmount (chatUser);

		int newDailyBilledAmount =
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
			@NonNull Optional<Integer> threadId) {

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
				dateToInstant (
					chatUser.getLastCreditHint ()),
				transaction.now ().minus (
					1000 * 60 * 60 * 24))

		) {

			// send message as appropriate

			Optional<ChatNetworkRec> chatNetworkOptional =
				chatNetworkHelper.forUser (
					chatUser);

			if (! chatNetworkOptional.isPresent ()) {

				log.warn (
					stringFormat (
						"Not sending credit hint to %s ",
						chatUser.getId (),
						"because no network settings found"));

				return;

			}

			ChatNetworkRec chatNetwork =
				chatNetworkOptional.get ();

			if (
				equal (
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
				equal (
					chatUser.getCreditMode (),
					ChatUserCreditMode.strict)
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
								Integer.toString (
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
					instantToDate (
						transaction.now ()));

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

		int targetLimit =
			sum (
				+ chatUser.getCreditSuccess (),
				- (chatUser.getCreditSuccess () % 1000) + 1000);

		// if it's already above that do nothing

		if (chatUser.getCreditLimit () >= targetLimit)
			return;

		// log it

		chatUserCreditLimitLogHelper.insert (
			new ChatUserCreditLimitLogRec ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

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
			chatUser.getCreditMode (),

			"credit:%s ",
			chatUser.getCredit (),

			"pending:%s ",
			chatUser.getCreditPending (),

			"strict:%s ",
			chatUser.getCreditPendingStrict (),

			"sent:%s ",
			chatUser.getCreditSent (),

			"revoked:%s ",
			chatUser.getCreditRevoked (),

			"retried:%s",
			chatUser.getCreditRetried ());

	}

}
