package wbs.apn.chat.user.image.api;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

@PrototypeComponent ("chatUserImageUploadErrorPage")
public
class ChatUserImageUploadErrorPage
	extends PrintResponder {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// state

	ChatUserImageUploadTokenRec imageUploadToken;
	ChatUserRec chatUser;

	String titleText;
	String bodyHtml;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		imageUploadToken =
			chatUserImageUploadTokenHelper.findByToken (
				requestContext.requestStringRequired (
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
	void goHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.addHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goContent");

		) {

			formatWriter.writeLineFormat (
				"<!DOCTYPE html>");

			formatWriter.writeLineFormatIncreaseIndent (
				"<html>");

			goHead (
				taskLogger);

			goBody (
				taskLogger);

			formatWriter.writeLineFormatDecreaseIndent (
				"</html>");

		}

	}

	protected
	void goHead (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goHead");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<head>");

			formatWriter.writeLineFormat (
				"<title>%h</title>",
				titleText);

			formatWriter.writeLineFormatDecreaseIndent (
				"</head>");

		}

	}

	protected
	void goBody (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter.writeLineFormatIncreaseIndent (
			"<body>");

		formatWriter.writeLineFormat (
			"<h1>%h</h1>",
			titleText);

		formatWriter.writeFormat (
			"%s\n",
			bodyHtml);

		formatWriter.writeLineFormatDecreaseIndent (
			"</body>");

	}

}
