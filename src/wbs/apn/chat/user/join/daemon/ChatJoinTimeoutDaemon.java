package wbs.apn.chat.user.join.daemon;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.Cleanup;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;

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

	// details

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			60);

	}

	@Override
	protected
	String generalErrorSource () {

		return "chat join timeout daemon";

	}

	@Override
	protected
	String generalErrorSummary () {

		return "error finding join messages to time out";

	}

	@Override
	protected
	String getThreadName () {

		return "ChatJoinTimeout";

	}

	// implementation

	@Override
	protected
	void runOnce () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatJoinTimeoutDaemon.runOnce ()",
				this);

		for (
			ChatRec chat
				: chatHelper.findAll ()
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
