package wbs.clients.apn.chat.user.join.daemon;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;

@SingletonComponent ("chatJoinTimeoutDaemon")
public
class ChatJoinTimeoutDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	Database database;

	// details

	@Override
	protected
	int getDelayMs () {

		return 60 * 1000;

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
			database.beginReadWrite ();

		for (ChatRec chat
				: chatHelper.findAll ()) {

			Instant createdTime =
				DateTime.now ()
					.minusSeconds (
						chat.getTimeSignupTimeout ())
					.toInstant ();

			List<ChatMessageRec> messages =
				chatMessageHelper.findSignupTimeout (
					chat,
					createdTime);

			for (ChatMessageRec message
					: messages) {

				message.setStatus (
					ChatMessageStatus.signupTimeout);

			}

		}

		transaction.commit ();

	}

}
