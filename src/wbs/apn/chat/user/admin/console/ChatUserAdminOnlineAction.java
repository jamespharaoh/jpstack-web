package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;

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

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserAdminOnlineAction")
public
class ChatUserAdminOnlineAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserAdminOnlineResponder")
	ComponentProvider <WebResponder> onlineResponderProvider;

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

			return onlineResponderProvider.provide (
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

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			String userType =
				capitalise (
					chatUser.getType ().name ());

			if (
				optionalIsPresent (
					requestContext.parameter (
						"online"))
			) {

				if (chatUser.getOnline ()) {

					requestContext.addNotice (
						stringFormat (
							"%s already online",
							userType));

					return null;

				}

				if (chatUser.getDeliveryMethod () == ChatMessageMethod.iphone) {

					requestContext.addWarning (
						"Can't change online status for iphone users");

					return null;

				}

				if (chatUser.getType () == ChatUserType.monitor) {

					chatUser

						.setOnline (
							true);

					eventLogic.createEvent (
						transaction,
						"chat_user_online",
						userConsoleLogic.userRequired (
							transaction),
						chatUser);

					transaction.commit ();

					requestContext.addNotice (
						"monitor brought online");

					return null;

				}

				/*
				if (chatUser.getFirstJoin () == null) {

					requestContext.addError (
						"user must complete signup process before joining");

					return null;

				}
				*/

				chatUser

					.setLastAction (
						transaction.now ());

				chatMiscLogic.userJoin (
					transaction,
					chatUser,
					true,
					optionalAbsent (),
					optionalFromNullable (
						chatUser.getDeliveryMethod ()));

				eventLogic.createEvent (
					transaction,
					"chat_user_online",
					userConsoleLogic.userRequired (
						transaction),
					chatUser);

				transaction.commit ();

				requestContext.addNotice (
					"user brought online");

				return null;

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"offline"))
			) {

				if (! chatUser.getOnline ()) {

					requestContext.addNotice (
						stringFormat (
							"%s already offline",
							userType));

					return null;

				}

				if (chatUser.getType () == ChatUserType.monitor) {

					chatUser

						.setOnline (
							false);

				} else {

					chatMiscLogic.userLogoffWithMessage (
						transaction,
						chatUser,
						optionalAbsent (),
						false);

				}

				eventLogic.createEvent (
					transaction,
					"chat_user_offline",
					userConsoleLogic.userRequired (
						transaction),
					chatUser);

				transaction.commit ();

				requestContext.addNotice (
					stringFormat (
						"%s taken offline",
						userType));

				return null;

			}

			throw new RuntimeException ();

		}

	}

}
