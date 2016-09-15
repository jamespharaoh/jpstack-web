package wbs.apn.chat.broadcast.logic;

import com.google.common.base.Optional;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("chatBroadcastLogic")
public
class ChatBroadcastLogicImplementation
	implements ChatBroadcastLogic {

	// singleton sdependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	// implementation

	@Override
	public
	boolean canSendToUser (
			ChatUserRec chatUser,
			boolean includeBlocked,
			boolean includeOptedOut) {

		// exclude incomplete users

		if (chatUser.getFirstJoin () == null)
			return false;

		// exclude blocked users, unless operator includes them

		if (
			chatUser.getBlockAll ()
			&& ! includeBlocked
		) {
			return false;
		}

		// exclude opted out users, unless operator includes them

		if (
			chatUser.getBroadcastOptOut ()
			&& ! includeOptedOut
		) {
			return false;
		}

		// exclude barred users

		if (chatUser.getBarred ())
			return false;

		// perform a credit check

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				false,
				Optional.<Long>absent ());

		if (
			creditCheckResult.failed ()
			&& creditCheckResult != ChatCreditCheckResult.failedBlocked
		) {
			return false;
		}

		// otherwise, include

		return true;

	}

}
