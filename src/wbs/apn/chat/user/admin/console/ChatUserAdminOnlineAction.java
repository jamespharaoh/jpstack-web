package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.instantToDate;

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

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminOnlineResponder");
	}

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext ("chat.userAdmin")) {
			requestContext.addError ("Access denied");
			return null;
		}

		String notice = null;

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

				notice = userType + " already online";

			} else if (chatUser.getDeliveryMethod () == ChatMessageMethod.iphone) {

				notice = "Can't change online status for iphone users";

			} else {

				if (chatUser.getType () == ChatUserType.monitor) {

					chatUser

						.setOnline (
							true);

				} else {

					chatUser

						.setLastAction (
							instantToDate (
								transaction.now ()));

					chatMiscLogic.userJoin (
						chatUser,
						true,
						null,
						chatUser.getDeliveryMethod ());

				}

				notice =
					userType + " brought online";

				eventLogic.createEvent (
					"chat_user_online",
					myUser,
					chatUser);

			}

		} else if (requestContext.parameter ("offline") != null) {

			if (chatUser.getOnline ()) {

				if (chatUser.getType () == ChatUserType.monitor) {
					chatUser.setOnline (false);
				} else {

					chatMiscLogic.userLogoffWithMessage (
						chatUser,
						null,
						false);

				}

				notice =
					userType + " taken offline";

				eventLogic.createEvent (
					"chat_user_offline",
					myUser,
					chatUser);

			} else {

				notice =
					userType + " already offline";

			}

		}

		transaction.commit ();

		if (notice != null)
			requestContext.addNotice (notice);

		return null;

	}

}
