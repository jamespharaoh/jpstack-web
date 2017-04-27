package wbs.apn.chat.user.join.daemon;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatJoinTimeoutDaemon")
public
class ChatJoinTimeoutDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "chat-user.join-timeout";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatJoinTimeoutDaemon.runOnce ()",
					this);

		) {

			for (
				ChatRec chat
					: chatHelper.findNotDeleted ()
			) {

				Instant createdTime =
					DateTime.now ()

					.minusSeconds (
						toJavaIntegerRequired (
							chat.getTimeSignupTimeout ()))

					.toInstant ();

				List<ChatMessageRec> messages =
					chatMessageHelper.findSignupTimeout (
						chat,
						createdTime);

				for (
					ChatMessageRec message
						: messages
				) {

					message.setStatus (
						ChatMessageStatus.signupTimeout);

				}

			}

			transaction.commit ();

		}

	}

}
