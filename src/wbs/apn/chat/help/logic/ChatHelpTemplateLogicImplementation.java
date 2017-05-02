package wbs.apn.chat.help.logic;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.misc.MapStringSubstituter;

import wbs.sms.gsm.MessageSplitter;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatTemplateLogic")
public
class ChatHelpTemplateLogicImplementation
	implements ChatHelpTemplateLogic {

	// singleton dependencies

	@SingletonDependency
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatHelpTemplateRec findChatHelpTemplate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull String type,
			@NonNull String code) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findChatHelpTemplate");

		) {

			ChatRec chat =
				chatUser.getChat ();

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			// try and find a scheme specific template

			if (chatScheme != null) {

				ChatHelpTemplateRec chatHelpTemplate =
					chatHelpTemplateHelper.findByTypeAndCode (
						transaction,
						chat,
						type,
						stringFormat (
							"%s_%s",
							code,
							chatScheme.getCode ()));

				if (chatHelpTemplate != null)
					return chatHelpTemplate;

			}

			// try and find a general template

			return chatHelpTemplateHelper.findByTypeAndCode (
				transaction,
				chat,
				type,
				code);

		}

	}

	@Override
	public
	MessageSplitter.Templates splitter (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull String templateCode,
			@NonNull Map <String, String> substitutions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"splitter");

		) {

			ChatHelpTemplateRec singleTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					templateCode + "_single");

			if (singleTemplate == null) {

				throw new RuntimeException (
					stringFormat (
						"No such system template %s ",
						templateCode + "_single",
						"for chat %s.%s",
						chat.getSlice ().getCode (),
						chat.getCode ()));

			}

			ChatHelpTemplateRec firstTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					templateCode + "_first");

			if (firstTemplate == null) {

				throw new RuntimeException (
					stringFormat (
						"No such system template %s ",
						templateCode + "_first",
						"for chat %s.%s",
						chat.getSlice ().getCode (),
						chat.getCode ()));

			}

			ChatHelpTemplateRec middleTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					templateCode + "_middle");

			if (middleTemplate == null) {

				throw new RuntimeException (
					stringFormat (
						"No such system template %s ",
						templateCode + "middle",
						"for chat %s.%s",
						chat.getSlice ().getCode (),
						chat.getCode ()));

			}

			ChatHelpTemplateRec lastTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					templateCode + "_last");

			if (lastTemplate == null) {

				throw new RuntimeException (
					stringFormat (
						"No such system template %s ",
						templateCode + "_last",
						"for chat %s.%s",
						chat.getSlice ().getCode (),
						chat.getCode ()));

			}

			return new MessageSplitter.Templates (

				MapStringSubstituter.substituteIgnoreMissing (
					singleTemplate.getText (),
					substitutions),

				MapStringSubstituter.substituteIgnoreMissing (
					firstTemplate.getText (),
					substitutions),

				MapStringSubstituter.substituteIgnoreMissing (
					middleTemplate.getText (),
					substitutions),

				MapStringSubstituter.substituteIgnoreMissing (
					lastTemplate.getText (),
					substitutions));

		}

	}

}
