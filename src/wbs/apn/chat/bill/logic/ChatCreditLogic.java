package wbs.apn.chat.bill.logic;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatCreditLogic {

	void userReceiveSpend (
			TaskLogger parentTaskLogger,
			ChatUserRec toUser,
			Long receivedMessageCount);

	void userSpend (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			int userMessageCount,
			int monitorMessageCount,
			int textProfileCount,
			int imageProfileCount,
			int videoProfileCount);

	void chatUserSpendBasic (
		ChatUserRec chatUser,
		int amount);

	ChatUserSpendRec findOrCreateChatUserSpend (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			LocalDate date);

	ChatCreditCheckResult userSpendCreditCheck (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			Boolean userActed,
			Optional <Long> threadId);

	ChatCreditCheckResult userCreditCheck (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser);

	ChatCreditCheckResult userCreditCheckStrict (
			ChatUserRec chatUser);

	Optional <String> userBillCheck (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			BillCheckOptions options);

	void userBill (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			BillCheckOptions options);

	void userBillReal (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			boolean updateRevoked);

	long userBillLimitAmount (
			ChatUserRec chatUser);

	boolean userBillLimitApplies (
			ChatUserRec chatUser);

	void userCreditHint (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			Optional <Long> threadId);

	void doRebill ();

	void creditLimitUpdate (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser);

	String userCreditDebug (
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
