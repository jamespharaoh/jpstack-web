package wbs.apn.chat.infosite.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

import javax.inject.Inject;

import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.PrintResponder;
import wbs.framework.web.RequestContext;

@PrototypeComponent ("chatInfoSiteViewResponder")
public
class ChatInfoSiteViewResponder
	extends PrintResponder {

	// dependencies

	@Inject
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@Inject
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@Inject
	RequestContext requestContext;

	// state

	ChatInfoSiteRec infoSite;
	ChatHelpTemplateRec infoSiteHelpTemplate;

	// implementation

	@Override
	protected
	void prepare () {

		infoSite =
			chatInfoSiteHelper.find (
				requestContext.requestInt ("chatInfoSiteId"));

		if (! equal (
			infoSite.getToken (),
			requestContext.request ("chatInfoSiteToken"))) {

			throw new RuntimeException ("Token mismatch");

		}

		infoSiteHelpTemplate =
			chatHelpTemplateHelper.findByTypeAndCode (
				infoSite.getChatUser ().getChat (),
				"system",
				"info_site_help");

	}

	@Override
	protected
	void goHeaders ()
		throws IOException {

		requestContext.addHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		printFormat (
			"<!DOCTYPE html>\n");

		printFormat (
			"<html>\n");

		goHead ();

		goBody ();

		printFormat (
			"</html>\n");

	}

	protected
	void goHead () {

		printFormat (
			"<head>\n");

		printFormat (
			"<title>%h</title>\n",
			"User profiles");

		printFormat (
			"</head>\n");

	}

	protected
	void goBody () {

		printFormat (
			"<body>\n");

		for (
			int index = 0;
			index < infoSite.getOtherChatUsers ().size ();
			index++
		) {

			ChatUserRec otherUser =
				infoSite.getOtherChatUsers ().get (index);

			if (index > 0) {

				printFormat (
					"<hr>\n");

			}

			printFormat (
				"<p>",

				"<a href=\"%h\">",
				stringFormat (
					"%u/%u/full",
					infoSite.getToken (),
					index),

				"<img src=\"%h\">",
				stringFormat (
					"%u/%u/normal",
					infoSite.getToken (),
					index),

				"</a>",

				"</p>\n");

			printFormat (
				"<p>User: %h</p>\n",
				otherUser.getCode ());

			if (otherUser.getName () != null) {

				printFormat (
					"<p>Name: %h</p>\n",
					otherUser.getName ());

			}

			if (otherUser.getInfoText () != null) {

				printFormat (
					"<p>Info: %h</p>\n",
					otherUser.getInfoText ().getText ());

			}

			printFormat (
				"<form method=\"post\">\n");

			printFormat (
				"<input " +
					"type=\"hidden\" " +
					"name=\"otherUserId\" " +
					"value=\"%h\">\n",
				otherUser.getId ());

			printFormat (
				"<p>\n",

				"<input " +
					"type=\"text\" " +
					"name=\"text\">\n",

				"<input " +
					"type=\"submit\" " +
					"value=\"send\">\n",

				"</p>\n");

			printFormat (
				"</form>\n");

		}

		printFormat (
			"%s\n",
			infoSiteHelpTemplate.getText ());

		printFormat (
			"</body>\n");

	}

}
