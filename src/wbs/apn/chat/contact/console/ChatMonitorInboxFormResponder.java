package wbs.apn.chat.contact.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlSpanWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.console.ChatUserAlarmConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserOperatorLabel;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatMonitorInboxFormResponder")
public
class ChatMonitorInboxFormResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatMonitorInboxConsoleHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatUserAlarmConsoleHelper chatUserAlarmHelper;

	@SingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatMonitorInboxRec chatMonitorInbox;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	ChatUserAlarmRec alarm;

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			Optional <ChatMonitorInboxRec> chatMonitorInboxOptional =
				chatMonitorInboxHelper.findFromContext (
					transaction);

			if (
				optionalIsNotPresent (
					chatMonitorInboxOptional)
			) {

				requestContext.addError (
					"Chat monitor inbox item not found");

				transaction.errorFormat (
					"Chat monitor inbox not found: %s",
					integerToDecimalString (
						requestContext.stuffIntegerRequired (
							"chatMonitorInboxId")));

				return;

			}

			chatMonitorInbox =
				chatMonitorInboxOptional.get ();

			userChatUser =
				chatMonitorInbox.getUserChatUser ();

			monitorChatUser =
				chatMonitorInbox.getMonitorChatUser ();

			alarm =
				chatUserAlarmHelper.find (
					transaction,
					userChatUser,
					monitorChatUser);

		}

	}

	@Override
	public
	void renderHtmlHeadContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContents");

		) {

			super.renderHtmlHeadContents (
				transaction,
				formatWriter);

			if (chatMonitorInbox == null)
				return;

			// script block open

			htmlScriptBlockOpen (
				formatWriter);

			// show inbox

			formatWriter.writeLineFormat (
				"top.show_inbox (true);");

			// set location

			formatWriter.writeLineFormat (
				"top.frames ['main'].location = '%j';",
				requestContext.resolveApplicationUrl (
					stringFormat (
						"%s",
						consoleManager
							.context (
								"chatMonitorInbox",
								true)
							.pathPrefix (),
						"/%u",
						integerToDecimalString (
							chatMonitorInbox.getId ()),
						"/chatMonitorInbox.summary")));

			// script block close

			htmlScriptBlockClose (
				formatWriter);

			// style block

			if (
				userChatUser.getOperatorLabel ()
					== ChatUserOperatorLabel.operator
			) {

				htmlStyleBlockOpen (
					formatWriter);

				htmlStyleRuleWrite (
					formatWriter,
					"h2",
					htmlStyleRuleEntry (
						"background",
						"#800000"));

				htmlStyleRuleWrite (
					formatWriter,
					"table.details th",
					htmlStyleRuleEntry (
						"background",
						"#800000"));

				htmlStyleBlockClose (
					formatWriter);

			}

		}

	}

	String doName (
			ChatUserRec chatUser) {

		if (chatUser.getName () != null) {
			return chatUser.getName () + " " + chatUser.getCode ();
		} else {
			return chatUser.getCode ();
		}

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContents");

		) {

			requestContext.flushNotices (
				formatWriter);

			// links

			htmlParagraphOpen (
				formatWriter,
				htmlClassAttribute (
					"links"));

			htmlLinkWrite (
				formatWriter,
				requestContext.resolveApplicationUrl (
					"/queues/queue.home"),
				"Queues");

			htmlLinkWrite (
				formatWriter,
				"javascript:top.show_inbox (false)",
				"Close");

			htmlParagraphClose (
				formatWriter);

			// handle deleted chat monitor inbox

			if (chatMonitorInbox == null)
				return;

			// send message

			htmlHeadingTwoWrite (
				formatWriter,
				stringFormat (
					"Send message as %s",
					userChatUser.getOperatorLabel ().toString ()));

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveApplicationUrl (
					stringFormat (
						"%s",
						consoleManager
							.context (
								"chatMonitorInbox",
								true)
							.pathPrefix (),
						"/%u",
						integerToDecimalString (
							chatMonitorInbox.getId ()),
						"/chatMonitorInbox.form")));

			// table open

			htmlTableOpenDetails (
				formatWriter);

			// from

			htmlTableDetailsRowWrite (
				formatWriter,
				"From",
				doName (
					monitorChatUser));

			// to

			htmlTableDetailsRowWrite (
				formatWriter,
				"To",
				doName (
					userChatUser));

			// message

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Message",
				() -> formatWriter.writeLineFormat (
					"<textarea",
					" name=\"text\"",
					" cols=\"64\"",
					" rows=\"4\"",
					" onkeyup=\"%h\"",
					stringFormat (
						"gsmCharCountMultiple2 (this, %s, %s);",
						"document.getElementById ('chars')",
						integerToDecimalString (
							ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
							* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES)),
					" onfocus=\"%h\"",
					stringFormat (
						"gsmCharCountMultiple2 (this, %s, %s);",
						"document.getElementById ('chars')",
						integerToDecimalString (
							ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
							* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES)),
					"></textarea>"));

			// chars

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Chars",
				() -> {

				htmlSpanWrite (
					formatWriter,
					"",
					htmlIdAttribute (
						"chars"));

				htmlSpanWrite (
					formatWriter,
					"",
					htmlIdAttribute (
						"messageCount"));

			});

			// table close

			htmlTableClose (
				formatWriter);

			// form controls

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"send\"",
				" value=\"send message\"",
				" onclick=\"%h\"",
				alarm == null
					? stringFormat (
						"return confirm ('%j');",
						"send message with no alarm (please ignore this message " +
						"if you already set one)")
					: "",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"sendAndNote\"",
				" value=\"send and make note\"",
				" onclick=\"%h\"",
				ifNotNullThenElse (
					alarm,
					() -> "",
					() -> stringFormat (
						"return confirm ('%j');",
						stringFormat (
							"send message with no alarm (please ignore this ",
							"message if you already set one)"))),
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"ignore\"",
				" value=\"don't send anything\"",
				" onclick=\"%h\"",
				ifNotNullThenElse (
					alarm,
					() -> "",
					() -> stringFormat (
						"return confirm ('%j');",
						stringFormat (
							"ignore message with no alarm (please ignore this ",
							"message if you already set one)"))),
				">");

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}