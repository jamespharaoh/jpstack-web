package wbs.apn.chat.broadcast.logic;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatBroadcastLogic")
public
class ChatBroadcastLogicImplementation
	implements ChatBroadcastLogic {

	// singleton sdependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	boolean canSendToUser (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser,
			boolean includeBlocked,
			boolean includeOptedOut) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"canSendToUser");

		) {

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
					taskLogger,
					chatUser,
					false,
					optionalAbsent ());

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

}
