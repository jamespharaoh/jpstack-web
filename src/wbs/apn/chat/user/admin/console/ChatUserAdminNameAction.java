package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalValueNotEqualSafe;
import static wbs.utils.string.StringUtils.nullIfEmptyString;

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

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.apn.chat.user.info.model.ChatUserNameObjectHelper;
import wbs.apn.chat.user.info.model.ChatUserNameRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserAdminNameAction")
public
class ChatUserAdminNameAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserNameObjectHelper chatUserNameHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserAdminNameResponder")
	ComponentProvider <WebResponder> nameResponderProvider;

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

			return nameResponderProvider.provide (
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

			if (
				! requestContext.canContext (
					"chat.userAdmin")
			) {

				requestContext.addError (
					"Access denied");

				return null;

			}

			Optional <ChatUserEditReason> editReasonOptional =
				toEnum (
					ChatUserEditReason.class,
					requestContext.parameterRequired (
						"editReason"));

			if (
				optionalIsNotPresent (
					editReasonOptional)
			) {

				requestContext.addError (
					"Please select a valid reason");

				return null;

			}

			ChatUserEditReason editReason =
				optionalGetRequired (
					editReasonOptional);

			String name =
				nullIfEmptyString (
					requestContext.parameterRequired (
						"name"));

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			if (
				optionalValueNotEqualSafe (
					optionalFromNullable (
						chatUser.getName ()),
					name)
			) {

				ChatUserNameRec chatUserName =
					chatUserNameHelper.insert (
						transaction,
						chatUserNameHelper.createInstance ()

					.setChatUser (
						chatUser)

					.setCreationTime (
						transaction.now ())

					.setOriginalName (
						chatUser.getName ())

					.setEditedName (
						name)

					.setModerator (
						userConsoleLogic.userRequired (
							transaction))

					.setStatus (
						ChatUserInfoStatus.console)

					.setEditReason (
						editReason)

				);

				chatUser.getChatUserNames ().add (
					chatUserName);

				chatUser

					.setName (
						name);

			}

			transaction.commit ();

			requestContext.addNotice (
				"Chat user name updated");

			requestContext.setEmptyFormData ();

			return null;

		}

	}

}
