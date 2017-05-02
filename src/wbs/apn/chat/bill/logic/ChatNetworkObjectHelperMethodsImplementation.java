package wbs.apn.chat.bill.logic;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.apn.chat.bill.model.ChatNetworkObjectHelperMethods;
import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatNetworkObjectHelperMethodsImplementation
	implements ChatNetworkObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	ChatNetworkObjectHelper chatNetworkHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <ChatNetworkRec> forUser (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"forUser");

		) {

			ChatNetworkRec chatNetwork =
				chatNetworkHelper.find (
					transaction,
					chatUser.getChat (),
					chatUser.getNumber ().getNetwork ());

			return Optional.fromNullable (
				chatNetwork);

		}

	}

}