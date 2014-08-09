package wbs.apn.chat.bill.logic;

import java.util.Date;

import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public interface ChatCreditLogic {

	void userReceiveSpend (
			ChatUserRec toUser,
			int receivedMessageCount);

	/**
	 * Bills a user the specified amount. This increases their valueSinceXxx
	 * counters and reduces their credit. If they have free usage it increases
	 * their creditAdded by the same amount and the overall credit is
	 * unaffected.
	 */
	void userSpend (
			ChatUserRec chatUser,
			int userMessageCount,
			int monitorMessageCount,
			int textProfileCount,
			int imageProfileCount,
			int videoProfileCount);

	void chatUserSpendBasic (
		ChatUserRec chatUser,
		int amount);

	/**
	 * @param chatUser
	 *            the chat user to associate with
	 * @param date
	 *            the date to associate with
	 * @return the new/existing chat user spend record
	 */
	ChatUserSpendRec findOrCreateChatUserSpend (
			ChatUserRec chatUser,
			Date date);

	/**
	 * Returns true if the user should receive non-billed content.
	 */
	boolean userReceiveCheck (ChatUserRec chatUser);

	boolean userSpendCheck (
			ChatUserRec chatUser,
			boolean userActed,
			Integer threadId,
			boolean allowBlocked);

	boolean userCreditOk (
			ChatUserRec chatUser,
			boolean allowBlocked);

	boolean userStrictCreditOk (ChatUserRec chatUser);

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
	void userBill (ChatUserRec chatUser, boolean retry);

	void userBillReal (
			ChatUserRec chatUser,
			boolean updateRevoked);

	int userBillLimitAmount (ChatUserRec chatUser);

	boolean userBillLimitApplies (ChatUserRec chatUser);

	/**
	 * Sends a credit hint to a chat user, unless they are barred or blocked or
	 * have had one very recently.
	 *
	 * @param chatUser
	 *            the chat user to send the hint to
	 * @param threadId
	 *            the threadId to associate the message with, or null
	 */
	void userCreditHint (
			ChatUserRec chatUser,
			Integer threadId);

	/**
	 * Returns a Date representing 0000 hours today.
	 *
	 * @return the date
	 */
	Date today ();

	void doRebill ();

	/**
	 * Checks if a user has a credit limit less than their successful delivered
	 * count rounded down to the nearest thousand (ten pounds) plus one thousand
	 * (ten pounds), if so it raises their credit limit to that amount and logs
	 * the event.
	 *
	 * @param chatUser
	 *            The user to check
	 */
	void creditLimitUpdate (
			ChatUserRec chatUser);

	String userCreditDebug (
			ChatUserRec chatUser);

}
