package wbs.imchat.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.platform.console.responder.HtmlResponder;
import wbs.platform.priv.console.PrivChecker;

@PrototypeComponent ("imChatMessagePendingFormResponder")
public
class ImChatMessagePendingFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	PrivChecker privChecker;

	// state

	ImChatMessageRec imChatMessage;

	String summaryUrl;

	boolean manager;

	// implementation

	@Override
	protected
	void prepare () {

		super.prepare ();

		imChatMessage =
			imChatMessageHelper.find (
				requestContext.stuffInt ("imChatMessageId"));

		summaryUrl =
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/imChatMessage.pending",
					"/%u",
					imChatMessage.getId (),
					"/imChatMessage.pending.history"));

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script language=\"JavaScript\">\n");

		printFormat (
			"top.show_inbox (true);\n",
			"top.frames ['main'].location = 'about:blank';\n",
			"window.setTimeout (function () { top.frames ['main'].location = '%j' }, 1);\n",
			summaryUrl);

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		requestContext.flushNotices (out);

		printFormat (
			"<p",
			" class=\"links\"",
			">\n",

			"<a",
			" href=\"%h\">Queues</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),

			"<a",
			" href=\"%h\"",
			summaryUrl,
			" target=\"main\"",
			">Summary</a>\n",

			"<a",
			" href=\"javascript:top.show_inbox (false);\"",
			">Close</a>\n",

			"</p>\n");

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/imChatMessage.pending",
					"/%u",
					imChatMessage.getId (),
					"/imChatMessage.pending.form")),
			" method=\"post\"",
			">\n");

		printFormat (
			"<input",
			" type=\"text\"",
			" name=\"reply\"",
			" value=\"Write your reply\"",
			">");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			">");

		printFormat (
			"</form>\n");

		printFormat (
			"<script language=\"JavaScript\">\n",
			"form_magic ();\n",
			"</script>\n");

	}

}
