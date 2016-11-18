package wbs.imchat.daemon;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.imchat.logic.ImChatLogic;
import wbs.imchat.model.ImChatConversationObjectHelper;
import wbs.imchat.model.ImChatConversationRec;

@Log4j
@SingletonComponent ("imChatConversationEmailDaemon")
public
class ImChatConversationEmailDaemon
	extends SleepingDaemonService {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatConversationObjectHelper imChatConversationHelper;

	@SingletonDependency
	ImChatLogic imChatLogic;

	// details

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			5);

	}

	@Override
	protected
	String generalErrorSource () {

		return "im chat conversation email daemon";

	}

	@Override
	protected
	String generalErrorSummary () {

		return "error finding conversations for email";

	}

	@Override
	protected
	String getThreadName () {

		return "ImChatConversationEmail";

	}

	// implementation

	@Override
	protected
	void runOnce () {

		log.debug (
			"Checking for conversations to send an email");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ImChatConversationEmailDaemon.runOnce ()",
				this);

		List<ImChatConversationRec> conversations =
			imChatConversationHelper.findPendingEmailLimit (
				1000);

		transaction.close ();

		for (
			ImChatConversationRec conversation
				: conversations
		) {

			doConversation (
				conversation.getId ());

		}

	}

	void doConversation (
			@NonNull Long conversationId) {

		@Cleanup
		Transaction updateTransaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s) begin",
					"ImChatConcersationEmailDaemon",
					"doConversation",
					stringFormat (
						"conversationId = %s",
						integerToDecimalString (
							conversationId))),
				this);

		ImChatConversationRec conversation =
			imChatConversationHelper.findRequired (
				conversationId);

		if (
			isNotNull (
				conversation.getEmailTime ())
		) {
			return;
		}

		conversation

			.setEmailTime (
				updateTransaction.now ());

		updateTransaction.commit ();

		@Cleanup
		Transaction emailTransaction =
			database.beginReadOnly (
				stringFormat (
					"%s.%s (%s) end",
					"ImChatConversationEmailDaemon",
					"doConversation",
					stringFormat (
						"conversationId = %s",
						integerToDecimalString (
							conversationId))),
				this);

		conversation =
			imChatConversationHelper.findRequired (
				conversationId);

		imChatLogic.conversationEmailSend (
			conversation);

	}

}
