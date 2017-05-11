package wbs.imchat.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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
		return "im-chat-conversation.email";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

		) {

			taskLogger.debugFormat (
				"Checking for conversations to send an email");

			List <Long> conversationIds =
				getConversationIds (
					taskLogger);

			conversationIds.forEach (
				conversationId ->
					doConversation (
						taskLogger,
						conversationId));

		}

	}

	private
	List <Long> getConversationIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			return iterableMapToList (
				imChatConversationHelper.findPendingEmailLimit (
					transaction,
					1000l),
				ImChatConversationRec::getId);

		}

	}

	void doConversation (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long conversationId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doConversation");

		) {

			doConversationUpdate (
				taskLogger,
				conversationId);

			doConversationEmail (
				taskLogger,
				conversationId);

		}

	}

	private
	void doConversationUpdate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long conversationId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithParameters (
					logContext,
					parentTaskLogger,
					"doConversationUpdate",
					keyEqualsDecimalInteger (
						"conversationId",
						conversationId));

		) {

			ImChatConversationRec conversation =
				imChatConversationHelper.findRequired (
					transaction,
					conversationId);

			if (
				isNotNull (
					conversation.getEmailTime ())
			) {
				return;
			}

			conversation

				.setEmailTime (
					transaction.now ());

			transaction.commit ();

		}

	}

	private
	void doConversationEmail (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long conversationId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithParameters (
					logContext,
					parentTaskLogger,
					"doConversationEmail",
					keyEqualsDecimalInteger (
						"conversationId",
						conversationId));

		) {

			ImChatConversationRec conversation =
				imChatConversationHelper.findRequired (
					transaction,
					conversationId);

			imChatLogic.conversationEmailSend (
				transaction,
				conversation);

		}

	}

}
