package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import lombok.Cleanup;

import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.action.ConsoleAction;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

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

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminOnlineResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext ("chat.userAdmin")) {
			requestContext.addError ("Access denied");
			return null;
		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminOnlineAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

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
					"chat_user_online",
					userConsoleLogic.userRequired (),
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
				chatUser,
				true,
				null,
				chatUser.getDeliveryMethod ());

			eventLogic.createEvent (
				"chat_user_online",
				userConsoleLogic.userRequired (),
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
					chatUser,
					null,
					false);

			}

			eventLogic.createEvent (
				"chat_user_offline",
				userConsoleLogic.userRequired (),
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
