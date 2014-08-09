package wbs.apn.chat.bill.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.sum;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.log4j.Log4j;

import org.joda.time.LocalDate;

import wbs.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.bill.model.ChatUserCreditLimitLogObjectHelper;
import wbs.apn.chat.bill.model.ChatUserCreditLimitLogRec;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.bill.model.ChatUserSpendObjectHelper;
import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.contact.logic.ChatSendLogic;
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
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Log4j
@SingletonComponent ("chatCreditLogic")
public
class ChatCreditLogicImpl
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
	ServiceObjectHelper serviceHelper;

	@Inject
	ObjectManager objectManager;

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

		ChatSchemeChargesRec chatSchemeCharges =
			toChatUser.getChatScheme ().getCharges ();

		// iphone users are never charged

		boolean free =
			toChatUser.getDeliveryMethod () == ChatMessageMethod.iphone;

		ChatUserSpendRec toChatUserSpend =
			findOrCreateChatUserSpend (toChatUser, today ());

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

		ChatSchemeChargesRec chatSchemeCharges =
			chatUser.getChatScheme ().getCharges ();

		// iphone users are never charged

		boolean free =
			chatUser.getDeliveryMethod () == ChatMessageMethod.iphone;

		// update the chat user spend

		ChatUserSpendRec chatUserSpend =
			findOrCreateChatUserSpend (
				chatUser,
				today ());

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
			Date date) {

		ChatUserSpendRec chatUserSpend =
			chatUserSpendHelper.findByDate (
				chatUser,
				date);

		if (chatUserSpend == null) {

			chatUserSpend =
				chatUserSpendHelper.insert (
					new ChatUserSpendRec ()
						.setChatUser (chatUser)
						.setDate (date));

		}

		return chatUserSpend;
	}

	/**
	 * Returns true if the user should receive non-billed content.
	 */
	@Override
	public
	boolean userReceiveCheck (
			ChatUserRec chatUser) {

		if (chatUser.getBlockAll ())
			return false;

		if (chatUser.getType () == ChatUserType.monitor)
			return true;

		return userCreditOk (
			chatUser,
			false);

	}

	@Override
	public
	boolean userSpendCheck (
			ChatUserRec chatUser,
			boolean userActed,
			Integer threadId,
			boolean allowBlocked) {

		log.debug (
			stringFormat (
				"userSpendCheck (%s, %s, %s, %s)",
				chatUser.getId (),
				userActed ? "yes" : "no",
				threadId,
				allowBlocked));

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

		if (
			userCreditOk (
				chatUser,
				allowBlocked)
		) {

			// credit ok, clear any credit hint

			chatUser

				.setLastCreditHint (
					null);

			// and return success

			return true;

		} else {

			// credit bad, if they acted send them a hint

			if (userActed)

				userCreditHint (
					chatUser,
					threadId);

			// log them off

			chatUserLogic.logoff (
				chatUser,
				threadId == null);

			// unschedule any ad

			chatUser.setNextAd (null);

			// and return failure

			return false;

		}

	}

	@Override
	public
	boolean userCreditOk (
			ChatUserRec chatUser,
			boolean allowBlocked) {

		log.debug (
			stringFormat (
				"userCreditOk (%s, %s)",
				chatUser.getId (),
				allowBlocked ? "true" : "false"));

		// deleted users always fail

		if (chatUser.getNumber () == null)
			return false;

		// blocked users fail unless the caller disables this check

		if (chatUser.getBlockAll () && ! allowBlocked)
			return false;

		// barred users always fail

		if (chatUser.getBarred ())
			return false;

		// further checks based on credit mode

		switch (chatUser.getCreditMode ()) {

		case normal:
			return true;

		case strict:
			return userStrictCreditOk (chatUser);

		case prePay:
			return userPrepayCreditOk (chatUser);

		case barred:
			return false;

		case free:
			return true;
		}

		throw new RuntimeException ();

	}

	@Override
	public
	boolean userStrictCreditOk (
			ChatUserRec chatUser) {

		// sanity check

		if (chatUser.getCreditMode () != ChatUserCreditMode.strict)
			throw new IllegalArgumentException ();

		// check network

		if (chatUser.getNumber ().getNetwork ().getId () == 0)
			return false;

		ChatNetworkRec chatNetwork =
			chatNetworkHelper.forUserRequired (
				chatUser);

		if (! chatNetwork.getAllowReverseBill ())
			return false;

		// can't check credit without a scheme

		if (chatUser.getChatScheme () == null)
			return true;

		// check credit

		int effectiveCredit =
			+ chatUser.getCredit ()
			- chatUser.getCreditPendingStrict ();

		ChatSchemeChargesRec charges =
			chatUser.getChatScheme ().getCharges ();

		return effectiveCredit >= - charges.getCreditLimit ()
				&& effectiveCredit >= - chatUser.getCreditLimit ();

	}

	boolean userPrepayCreditOk (
			ChatUserRec chatUser) {

		// sanity check

		if (chatUser.getCreditMode () != ChatUserCreditMode.prePay)
			throw new IllegalArgumentException ();

		// check network

		if (chatUser.getNumber ().getNetwork ().getId () == 0)
			return false;

		ChatNetworkRec chatNetwork =
			chatNetworkHelper.forUserRequired (
				chatUser);

		if (! chatNetwork.getAllowPrePay ())
			return false;

		// check prepay credit

		return chatUser.getCredit () >= 0;

	}

	/**
	 * Bills the user by sending allMessages, if appropriate.
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

		String chatUserPath =
			objectManager.objectPath (
				chatUser,
				null,
				true);

		if (chatUser.getNumber () == null) {

			log.warn ("Unable to bill user " + chatUser.getId () + " with no number");

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

		// check they are on normal or strict billing

		if (chatUser.getCreditMode () != ChatUserCreditMode.normal
				&& chatUser.getCreditMode () != ChatUserCreditMode.strict) {

			log.debug (
				stringFormat (
					"Ignoring credit request for %s as their credit mode is %s",
					chatUser.getId (),
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
					"rejecting bill message for user %s due to daily limit",
					chatUserPath));

			return;

		}

		// work out start of day

		GregorianCalendar startOfDay =
			new GregorianCalendar ();

		startOfDay.set (Calendar.HOUR_OF_DAY, 0);
		startOfDay.set (Calendar.MINUTE, 0);
		startOfDay.set (Calendar.SECOND, 0);
		startOfDay.set (Calendar.MILLISECOND, 0);

		Date startOfDayDate =
			startOfDay.getTime ();

		// check user hasnt been sent a bill today that wasn't successful

		if (
			chatUser.getLastBillSent () != null
			&& ! chatUser.getLastBillSent ().before (
				startOfDayDate)
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

				chatUser.incCreditPendingStrict (route.getOutCharge ());

			} else {

				chatUser.incCreditPending (route.getOutCharge ());

			}

		} else {

			chatUser.incCreditSent (route.getOutCharge ());

		}

		// update revoked if appropriate

		boolean revoked =
			updateRevoked && (
				+ chatUser.getCredit ()
				- route.getOutCharge ()
				+ chatUser.getCreditRevoked ()
			) >= 0;

		if (revoked) {

			chatUser.incCreditRevoked (
				- route.getOutCharge ());

			chatUser.incCreditRetried (
				+ route.getOutCharge ());

			if (chatUser.getCreditRevoked () < 0) {

				chatUser.incCreditRetried (
					chatUser.getCreditRevoked ());

				chatUser.incCreditRevoked (
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

		String message =
			chatHelpTemplate.getText ();

		// TODO Mediaburst switchover - remove once O2 catches up
		// Hard coded route switch for mediaburst change-over, put back to
		// normal when O2 switchover is complete
		// Network ID 4 is O2 network ID
		// route ID 68 is new 88211 route, 33 is old 88211 route
		// route ID 69 is new 84469 route, 41 is old 84469 route

		// and send it

		ServiceRec billService =
			serviceHelper.findByCode (chat, "bill");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (chatUser);

		messageSender.get ()

			.number (
				chatUser.getNumber ())

			.messageString (
				message)

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

			.incDailyBilledAmount (
				route.getOutCharge ());

		/*
		 * if (APPLY_BILL_LIMIT) { if
		 * (chatUser.getDailyBilledStart().before(testDate)) {
		 * chatUser.setDailyBilledStart(new GregorianCalendar()); } }
		 */

		chatUser

			.setLastBillSent (
				new Date ());

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

		ChatSchemeChargesRec charges =
			chatUser.getChatScheme ().getCharges ();

		if (! charges.getBillLimitEnabled ())
			return false;

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// work out what day we're on

		GregorianCalendar startOfDay =
			new GregorianCalendar ();

		startOfDay.set (
			Calendar.HOUR_OF_DAY,
			0);

		startOfDay.set (
			Calendar.MINUTE,
			0);

		startOfDay.set (
			Calendar.SECOND,
			0);

		startOfDay.set (
			Calendar.MILLISECOND,
			0);

		Date startOfDayDate =
			startOfDay.getTime ();

		// reset the limit on a new day

		if (
			chatUser.getCreditDailyDate () == null
			|| chatUser.getCreditDailyDate ().toDate ().before (
				startOfDayDate)
		) {

			chatUser

				.setCreditDailyDate (
					new LocalDate (startOfDayDate))

				.setDailyBilledAmount (
					0);

		}

		// apply the limit

		int routeCharge =
			chatScheme.getRbBillRoute ().getOutCharge ();

		int limit =
			userBillLimitAmount (chatUser);

		int newDailyBilledAmount =
			+ chatUser.getDailyBilledAmount ()
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
			ChatUserRec chatUser,
			Integer threadId) {

		Date now = new Date ();

		if (chatUser.getBarred ())
			return;

		if (chatUser.getBlockAll ())
			return;

		if (chatUser.getLastCreditHint () == null
				|| chatUser.getLastCreditHint ().getTime ()
					< now.getTime () - 1000 * 60 * 60 * 24) {

			// send message as appropriate

			ChatNetworkRec chatNetwork =
				chatNetworkHelper.forUserRequired (
					chatUser);

			if (
				equal (
					chatUser.getCreditMode (),
					ChatUserCreditMode.prePay)
			) {

				if (! chatNetwork.getAllowPrePay ()) {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						Optional.of (threadId),
						"credit_hint_network",
						Collections.<String,String>emptyMap ());

				} else {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						Optional.of (threadId),
						"credit_hint_prepay",
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
						Optional.of (threadId),
						"credit_hint_network",
						Collections.<String,String>emptyMap ());

				} else if (
					userBillLimitApplies (
						chatUser)
				) {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						Optional.of (threadId),
						"credit_hint_daily",
						ImmutableMap.<String,String>builder ()

							.put (
								"limit",
								Integer.toString (
									userBillLimitAmount (chatUser) / 100))

							.build ());

				} else {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						Optional.of (threadId),
						"credit_hint",
						Collections.<String,String>emptyMap ());

				}

			} else {

				throw new RuntimeException ();

			}

			chatUser

				.setLastCreditHint (
					now);

		}

	}

	/**
	 * Returns a Date representing 0000 hours today.
	 *
	 * @return the date
	 */
	@Override
	public
	Date today () {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.set (
			Calendar.HOUR,
			0);

		calendar.set (
			Calendar.MINUTE,
			0);

		calendar.set (
			Calendar.SECOND,
			0);

		calendar.set (
			Calendar.MILLISECOND,
			0);

		return calendar.getTime ();

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
				new Date ())

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
