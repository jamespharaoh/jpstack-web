package wbs.apn.chat.user.image.api;

import java.io.IOException;

import javax.inject.Inject;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.PrintResponder;
import wbs.framework.web.RequestContext;

import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("chatUserImageUploadErrorPage")
public
class ChatUserImageUploadErrorPage
	extends PrintResponder {

	// dependencies

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	RequestContext requestContext;

	// state

	ChatUserImageUploadTokenRec imageUploadToken;
	ChatUserRec chatUser;

	String titleText;
	String bodyHtml;

	// implementation

	@Override
	protected
	void prepare () {

		imageUploadToken =
			chatUserImageUploadTokenHelper.findByToken (
				(String)
				requestContext.request (
					"chatUserImageUploadToken"));

		chatUser =
			imageUploadToken.getChatUser ();

		titleText =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_error_title",
				ImmutableMap.<String,String> of ());

		bodyHtml =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_error_body",
				ImmutableMap.<String,String> of ());

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
			titleText);

		printFormat (
			"</head>\n");

	}

	protected
	void goBody () {

		printFormat (
			"<body>\n");

		printFormat (
			"<h1>%h</h1>\n",
			titleText);

		printFormat (
			"%s\n",
			bodyHtml);

		printFormat (
			"</body>\n");

	}

}
