package wbs.apn.chat.bill.daemon;

import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Named;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@SingletonDependency
	@Named
	SymbolicLock <Long> chatUserDeliveryLocks;

	// details

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return Arrays.asList (
			"chat_bill",
			"chat_bill_strict");

	}

	// implementation

	private
	void addCredit (
			ChatUserRec chatUser,
			MessageStatus status,
			long amount,
			boolean today,
			boolean strict) {

		if (status.isGoodType ()) {

			chatUser

				.setCreditSuccess (
					+ chatUser.getCreditSuccess ()
					+ amount);

		} else if (status.isBadType ()) {

			if (today && (amount > 0)) {

				chatUser

					.setCreditDailyAmount (
						+ chatUser.getCreditDailyAmount ()
						- amount);

			}

			if (strict) {

				chatUser

					.setCreditRevoked (
						+ chatUser.getCreditRevoked ()
						+ amount);

				if (chatUser.getCreditMode () != ChatUserCreditMode.prePay) {

					chatUser

						.setCredit (
							chatUser.getCredit () - amount);

				}

			} else {

				chatUser

					.setCreditFailed (
						+ chatUser.getCreditFailed ()
						+ amount);

			}

		} else {

			if (strict) {

				chatUser

					.setCreditPendingStrict (
						+ chatUser.getCreditPendingStrict ()
						+ amount);

			} else {

				chatUser

					.setCreditPending (
						+ chatUser.getCreditPending ()
						+ amount);

			}

		}

	}

	@Override
	public
	void handle (
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		@Cleanup
		HeldLock lock =
			chatUserDeliveryLocks.easy (
				ref);

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatBillDeliveryHandler.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		DeliveryTypeRec deliveryType =
			message.getDeliveryType ();

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				message.getRef ());

		// work out strict mode

		boolean strict;

		if (
			stringEqualSafe (
				deliveryType.getCode (),
				"chat_bill")
		) {

			strict = false;

		} else if (
			stringEqualSafe (
				deliveryType.getCode (),
				"chat_bill_strict")
		) {

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
			message.getCreatedTime ();

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
				"Delivery report processed for message %s %s ",
				message.getId (),
				delivery.getNewMessageStatus (),
				"for chat user %s %s",
				chatUser.getId (),
				chatCreditLogic.userCreditDebug (chatUser)));

		transaction.commit ();

	}

}
