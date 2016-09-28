package wbs.apn.chat.user.core.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlSpanWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatUserHelpFormResponder")
public
class ChatUserHelpFormResponder
	extends HtmlResponder {

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
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContents () {

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
