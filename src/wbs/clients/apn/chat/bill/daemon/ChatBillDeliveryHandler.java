package wbs.clients.apn.chat.bill.daemon;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.misc.SymbolicLock;
import wbs.platform.misc.SymbolicLock.HeldLock;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.delivery.model.DeliveryTypeRec;

@PrototypeComponent ("chatBillDeliveryHandler")
@Log4j
public
class ChatBillDeliveryHandler
	implements DeliveryHandler {

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject @Named
	SymbolicLock<Integer> chatUserDeliveryLocks;

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return Arrays.asList (
			"chat_bill",
			"chat_bill_strict");

	}

	private
	void addCredit (
			ChatUserRec chatUser,
			MessageStatus status,
			int amount,
			boolean today,
			boolean strict) {

		if (status.isGoodType ()) {

			chatUser.incCreditSuccess (amount);

		} else if (status.isBadType ()) {

			if (today && (amount > 0)) {

				chatUser

					.setCreditDailyAmount (
						+ chatUser.getCreditDailyAmount ()
						- amount);

			}

			if (strict) {

				chatUser.incCreditRevoked (amount);

				if (chatUser.getCreditMode () != ChatUserCreditMode.prePay) {

					chatUser

						.setCredit (
							chatUser.getCredit () - amount);

				}

			} else {

				chatUser.incCreditFailed (amount);

			}

		} else {

			if (strict) {

				chatUser.incCreditPendingStrict (amount);

			} else {

				chatUser.incCreditPending (amount);

			}

		}

	}

	@Override
	public
	void handle (
			int deliveryId,
			Integer ref) {

		@Cleanup
		HeldLock lock =
			chatUserDeliveryLocks.easy (
				ref);

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		DeliveryTypeRec deliveryType =
			message.getDeliveryType ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				message.getRef ());

		// work out strict mode

		boolean strict;

		if (equal (
				deliveryType.getCode (),
				"chat_bill")) {

			strict = false;

		} else if (equal (
				deliveryType.getCode (),
				"chat_bill_strict")) {

			strict = true;

		} else {

			throw new RuntimeException (
				deliveryType.getCode ());

		}

		// work out if sent today

		DateTimeZone timeZone =
			DateTimeZone.forID (
				chatUser.getChatScheme ().getTimezone ());

		Instant startOfDayTime =
			DateTime
				.now (timeZone)
				.withTimeAtStartOfDay ()
				.toInstant ();

		Instant messageSentTime =
			dateToInstant (
				message.getCreatedTime ());

		boolean sentToday =
			messageSentTime.isAfter (
				startOfDayTime);

		// update last bill sent

		if (
			sentToday
			&& delivery.getNewMessageStatus ().isGoodType ()
		) {

			chatUser

				.setLastBillSent (
				null);

		}

		addCredit (
			chatUser,
			delivery.getOldMessageStatus (),
			- message.getCharge (),
			sentToday,
			strict);

		addCredit (
			chatUser,
			delivery.getNewMessageStatus (),
			message.getCharge (),
			sentToday,
			strict);

		// and remove the delivery

		deliveryHelper.remove (
			delivery);

		// and rebill if appropriate

		if (delivery.getNewMessageStatus ().isGoodType ()) {

			chatCreditLogic.userBill (
				chatUser,
				true);

		}

		// update credit limit where appropriate

		if (delivery.getNewMessageStatus ().isGoodType ()) {

			chatCreditLogic.creditLimitUpdate (
				chatUser);

		}

		log.info (
			stringFormat (
				"Delivery report processed for message %s %s for chat user %s %s",
				message.getId (),
				delivery.getNewMessageStatus (),
				chatUser.getId (),
				chatCreditLogic.userCreditDebug (chatUser)));

		transaction.commit ();

	}

}
