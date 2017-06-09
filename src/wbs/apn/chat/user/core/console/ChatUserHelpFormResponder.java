package wbs.apn.chat.user.core.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlSpanWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserHelpFormResponder")
public
class ChatUserHelpFormResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatUserRec chatUser;

	// implementation

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

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

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

			htmlHeadingTwoWrite (
				formatWriter,
				"Send help message");

			requestContext.flushNotices (
				formatWriter);

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatUser.helpForm"));

			htmlTableOpenDetails (
				formatWriter);

			String userInfo =
				chatUser.getName () == null
					? chatUser.getCode ()
					: chatUser.getCode () + " " + chatUser.getName ();

			htmlTableDetailsRowWrite (
				formatWriter,
				"User",
				userInfo);

			String charCountScript =
				stringFormat (
					"gsmCharCount (%s, %s, 149)",
					"document.getElementById ('text')",
					"document.getElementById ('chars')");

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Message",
				() -> formatWriter.writeLineFormat (
					"<textarea",
					" id=\"text\"",
					" cols=\"64\"",
					" rows=\"4\"",
					" name=\"text\"",
					" onkeyup=\"%h\"",
					charCountScript,
					" onfocus=\"%h\"",
					charCountScript,
					"></textarea>"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Chars",
				() -> htmlSpanWrite (
					formatWriter,
					"",
					htmlIdAttribute (
						"chars")));

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"send message\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

			htmlScriptBlockWrite (
				formatWriter,
				stringFormat (
					"%s;",
					charCountScript));

		}

	}

}
