package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.Misc;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserAdminOnlineAction")
public
class ChatUserAdminOnlineAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	UserObjectHelper userHelper;

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
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		String userType =
			Misc.capitalise (chatUser.getType ().name ());

		if (requestContext.parameter ("online") != null) {

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
					myUser,
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
					instantToDate (
						transaction.now ()));

			chatMiscLogic.userJoin (
				chatUser,
				true,
				null,
				chatUser.getDeliveryMethod ());

			eventLogic.createEvent (
				"chat_user_online",
				myUser,
				chatUser);

			transaction.commit ();

			requestContext.addNotice (
				"user brought online");

			return null;

		}

		if (requestContext.parameter ("offline") != null) {

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
				myUser,
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
