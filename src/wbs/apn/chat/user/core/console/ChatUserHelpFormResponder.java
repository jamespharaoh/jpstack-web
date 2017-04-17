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

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatUserHelpFormResponder")
public
class ChatUserHelpFormResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findFromContextRequired ();

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		htmlHeadingTwoWrite (
			"Send help message");

		requestContext.flushNotices (
			formatWriter);

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.helpForm"));

		htmlTableOpenDetails ();

		String userInfo =
			chatUser.getName () == null
				? chatUser.getCode ()
				: chatUser.getCode () + " " + chatUser.getName ();

		htmlTableDetailsRowWrite (
			"User",
			userInfo);

		String charCountScript =
			stringFormat (
				"gsmCharCount (%s, %s, 149)",
				"document.getElementById ('text')",
				"document.getElementById ('chars')");

		htmlTableDetailsRowWriteHtml (
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
			"Chars",
			() -> htmlSpanWrite (
				"",
				htmlIdAttribute (
					"chars")));

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"send message\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

		htmlScriptBlockWrite (
			stringFormat (
				"%s;",
				charCountScript));

	}

}
