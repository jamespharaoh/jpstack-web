package wbs.apn.chat.adult.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		taskLogger.debugFormat (
			"Checking for all users whose adult verification has expired");

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatAdultExpiryDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> chatUsers =
				chatUserHelper.findAdultExpiryLimit (
					transaction.now (),
					1000);

			transaction.close ();

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				doUserAdultExpiry (
					taskLogger,
					chatUser.getId ());

			}

		}

	}

	void doUserAdultExpiry (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"doUserAdultExpiry (%s)",
				integerToDecimalString (
					chatUserId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatAdultExpiryDaemon.runOnce ()",
					this);

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			String chatUserPath =
				objectManager.objectPath (
					chatUser);

			// make sure adult expiry is set

			if (chatUser.getAdultExpiry () == null) {

				taskLogger.warningFormat (
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

				taskLogger.warningFormat (
					"Skipped adult expiry for %s ",
					chatUserPath,
					"(time is in future)");

				return;

			}

			taskLogger.noticeFormat (
				"Performing adult expiry for %s (time is %s)",
				chatUserPath,
				chatUser.getAdultExpiry ().toString ());

			// update the user

			chatUser.setAdultVerified (false);
			chatUser.setAdultExpiry (null);

			if (chatUser.getBlockAll ()) {

				taskLogger.noticeFormat (
					"Not sending adult expiry message to %s due to block all",
					chatUserPath);

			} else if (chatUser.getNumber () == null) {

				taskLogger.noticeFormat (
					"Not sending adult expiry message to %s due to deletion",
					chatUserPath);

			} else if (chatUser.getChatScheme () == null) {

				taskLogger.noticeFormat (
					"Not sending adult expiry message to %s ",
					chatUserPath,
					"due to lack of scheme");

			} else {

				// send them a message

				if (chatScheme.getRbFreeRouter () != null) {

					chatSendLogic.sendSystemRbFree (
						taskLogger,
						chatUser,
						optionalAbsent (),
						"adult_expiry",
						TemplateMissing.error,
						emptyMap ());

				} else {

					taskLogger.warningFormat (
						"Not sending adult expiry to %s ",
						chatUserPath,
						"as no route is configured");

				}

			}

			transaction.commit ();

		}

	}

}
