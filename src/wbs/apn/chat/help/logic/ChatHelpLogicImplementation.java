package wbs.apn.chat.help.logic;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatHelpLogic")
public
class ChatHelpLogicImplementation
	implements ChatHelpLogic {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void sendHelpMessage (
			@NonNull Transaction parentTransaction,
			@NonNull UserRec user,
			@NonNull ChatUserRec chatUser,
			@NonNull String text,
			@NonNull Optional <Long> originalThreadId,
			@NonNull Optional <ChatHelpLogRec> replyTo) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendHelpMessage");

		) {

			Optional <Long> threadId =
				originalThreadId;

			ChatRec chat =
				chatUser.getChat ();

			CommandRec magicCommand =
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"magic");

			CommandRec helpCommand =
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"help");

			ServiceRec helpService  =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"help");

			// load templates

			ChatHelpTemplateRec singleTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					"help_single");

			ChatHelpTemplateRec multipleTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					"help_multiple");

			// split message

			MessageSplitter.Templates messageSplitterTemplates =
				new MessageSplitter.Templates (
					singleTemplate.getText (),
					multipleTemplate.getText (),
					multipleTemplate.getText (),
					multipleTemplate.getText ());

			List <String> splitTexts =
				MessageSplitter.split (
					text,
					messageSplitterTemplates);

			for (
				String splitText
					: splitTexts
			) {

				// send message

				MessageRec message =
					chatSendLogic.sendMessageMagic (
						transaction,
						chatUser,
						threadId,
						textHelper.findOrCreate (
							transaction,
							splitText),
						magicCommand,
						helpService,
						helpCommand.getId ());

				if (
					optionalIsNotPresent (
						threadId)
				) {

					threadId =
						Optional.of (
							message.getId ());

				}

				// save reply

				chatHelpLogLogic.createChatHelpLogOut (
					transaction,
					chatUser,
					replyTo,
					optionalOf (
						user),
					message,
					optionalAbsent (),
					splitText,
					optionalOf (
						helpCommand));

			}

		}

	}

}
