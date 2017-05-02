package wbs.apn.chat.bill.logic;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.framework.database.Transaction;

import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatCreditLogic {

	void userReceiveSpend (
			Transaction parentTransaction,
			ChatUserRec toUser,
			Long receivedMessageCount);

	void userSpend (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			int userMessageCount,
			int monitorMessageCount,
			int textProfileCount,
			int imageProfileCount,
			int videoProfileCount);

	void chatUserSpendBasic (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			int amount);

	ChatUserSpendRec findOrCreateChatUserSpend (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			LocalDate date);

	ChatCreditCheckResult userSpendCreditCheck (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Boolean userActed,
			Optional <Long> threadId);

	ChatCreditCheckResult userCreditCheck (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	ChatCreditCheckResult userCreditCheckStrict (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	Optional <String> userBillCheck (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			BillCheckOptions options);

	void userBill (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			BillCheckOptions options);

	void userBillReal (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			boolean updateRevoked);

	long userBillLimitAmount (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	boolean userBillLimitApplies (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	void userCreditHint (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId);

	void doRebill (
			Transaction parentTransaction);

	void creditLimitUpdate (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	String userCreditDebug (
			Transaction parentTransaction,
			ChatUserRec chatUser);

	@Accessors (fluent = true)
	@Data
	public static
	class BillCheckOptions {
		Boolean retry = false;
		Boolean includeBlocked = false;
		Boolean includeFailed = false;
	}

}
