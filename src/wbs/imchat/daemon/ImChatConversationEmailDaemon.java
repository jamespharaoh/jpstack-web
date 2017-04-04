package wbs.imchat.daemon;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.imchat.logic.ImChatLogic;
import wbs.imchat.model.ImChatConversationObjectHelper;
import wbs.imchat.model.ImChatConversationRec;

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

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "im-chat.conversation-email";
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
			"Checking for conversations to send an email");

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ImChatConversationEmailDaemon.runOnce ()",
					this);

		) {

			List <ImChatConversationRec> conversations =
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

	}

	void doConversation (
			@NonNull Long conversationId) {

		try (

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

		) {

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

			try (

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

			) {

				conversation =
					imChatConversationHelper.findRequired (
						conversationId);

				imChatLogic.conversationEmailSend (
					conversation);

			}

		}

	}

}
