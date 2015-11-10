package wbs.clients.apn.chat.broadcast.logic;

import javax.inject.Inject;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatBroadcastLogic")
public
class ChatBroadcastLogicImplementation
	implements ChatBroadcastLogic {

	// dependencies

	@Inject
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
				Optional.<Integer>absent ());

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
