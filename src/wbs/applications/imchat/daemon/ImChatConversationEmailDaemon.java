package wbs.applications.imchat.daemon;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.applications.imchat.logic.ImChatLogic;
import wbs.applications.imchat.model.ImChatConversationObjectHelper;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;

@Log4j
@SingletonComponent ("imChatConversationEmailDaemon")
public
class ImChatConversationEmailDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatConversationObjectHelper imChatConversationHelper;

	@Inject
	ImChatLogic imChatLogic;

	// details

	@Override
	protected
	int getDelayMs () {

		return 5 * 1000;

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
				this);

		List<ImChatConversationRec> conversations =
			imChatConversationHelper.findPendingEmail (
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
			int conversationId) {

		@Cleanup
		Transaction updateTransaction =
			database.beginReadWrite (
				this);

		ImChatConversationRec conversation =
			imChatConversationHelper.find (
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
				this);

		conversation =
			imChatConversationHelper.find (
				conversationId);

		imChatLogic.conversationEmailSend (
			conversation);

	}

}
