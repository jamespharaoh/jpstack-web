package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserAdminCreditModeAction")
public
class ChatUserAdminCreditModeAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserAdminCreditModeResponder")
	ComponentProvider <WebResponder> creditModeResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return creditModeResponderProvider.provide (
				taskLogger);

		}

	}

	// implementation

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// check privs

			if (! requestContext.canContext (
					"chat.userCredit")) {

				requestContext.addError (
					"Access denied");

				return null;

			}

			// get params

			Optional <ChatUserCreditMode> newCreditModeOptional =
				toEnum (
					ChatUserCreditMode.class,
					requestContext.parameterRequired (
						"creditMode"));

			if (
				optionalIsNotPresent (
					newCreditModeOptional)
			) {

				requestContext.addError (
					"Please select a valid credit mode");

				return null;

			}

			ChatUserCreditMode newCreditMode =
				optionalGetRequired (
					newCreditModeOptional);

			// lookup objects

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			ChatUserCreditMode oldCreditMode =
				chatUser.getCreditMode ();

			// if it changed

			if (newCreditMode != oldCreditMode) {

				// update chat user

				chatUserLogic.creditModeChange (
					transaction,
					chatUser,
					newCreditMode);

				// and log event

				eventLogic.createEvent (
					transaction,
					"chat_user_credit_mode",
					userConsoleLogic.userRequired (
						transaction),
					chatUser,
					oldCreditMode.toString (),
					newCreditMode.toString ());

			}

			transaction.commit ();

			// we're done

			requestContext.addNotice (
				"Credit mode updated");

			return null;

		}

	}

}
