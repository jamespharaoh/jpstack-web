package wbs.apn.chat.adult.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatAdultExpiryDaemon")
public
class ChatAdultExpiryDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "chat-user.adult-expiry";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			transaction.debugFormat (
				"Checking for all users whose adult verification has expired");

			List <ChatUserRec> chatUsers =
				chatUserHelper.findAdultExpiryLimit (
					transaction,
					transaction.now (),
					1000l);

			transaction.close ();

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				doUserAdultExpiry (
					transaction,
					chatUser.getId ());

			}

		}

	}

	void doUserAdultExpiry (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					transaction,
					chatUserId);

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			String chatUserPath =
				objectManager.objectPath (
					transaction,
					chatUser);

			// make sure adult expiry is set

			if (chatUser.getAdultExpiry () == null) {

				transaction.warningFormat (
					"Skipped adult expiry for %s ",
					chatUserPath,
					"(field is null)");

				return;

			}

			// make sure adult expiry time is in past

			if (
				earlierThan (
					transaction.now (),
					chatUser.getAdultExpiry ())
			) {

				transaction.warningFormat (
					"Skipped adult expiry for %s ",
					chatUserPath,
					"(time is in future)");

				return;

			}

			transaction.noticeFormat (
				"Performing adult expiry for %s (time is %s)",
				chatUserPath,
				chatUser.getAdultExpiry ().toString ());

			// update the user

			chatUser.setAdultVerified (false);
			chatUser.setAdultExpiry (null);

			if (chatUser.getBlockAll ()) {

				transaction.noticeFormat (
					"Not sending adult expiry message to %s due to block all",
					chatUserPath);

			} else if (chatUser.getNumber () == null) {

				transaction.noticeFormat (
					"Not sending adult expiry message to %s due to deletion",
					chatUserPath);

			} else if (chatUser.getChatScheme () == null) {

				transaction.noticeFormat (
					"Not sending adult expiry message to %s ",
					chatUserPath,
					"due to lack of scheme");

			} else {

				// send them a message

				if (chatScheme.getRbFreeRouter () != null) {

					chatSendLogic.sendSystemRbFree (
						transaction,
						chatUser,
						optionalAbsent (),
						"adult_expiry",
						TemplateMissing.error,
						emptyMap ());

				} else {

					transaction.warningFormat (
						"Not sending adult expiry to %s ",
						chatUserPath,
						"as no route is configured");

				}

			}

			transaction.commit ();

		}

	}

}
