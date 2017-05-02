package wbs.imchat.console;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.user.console.UserConsoleHelper;

import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatMessageSearch;

@SingletonComponent ("imChatMessageConsoleHooks")
public
class ImChatMessageConsoleHooks
	implements ConsoleHooks <ImChatMessageRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Transaction parentTransaction,
			@NonNull Object searchObject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"applySearchFilter");

		) {

			ImChatMessageSearch search =
				genericCastUnchecked (
					searchObject);

			search

				.imChatId (
					optionalOrNull (
						requestContext.stuffInteger (
							"imChatId")))

			;

		}

	}

}
