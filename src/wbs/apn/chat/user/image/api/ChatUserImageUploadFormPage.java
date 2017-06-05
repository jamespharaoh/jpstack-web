package wbs.apn.chat.user.image.api;

import static wbs.utils.collection.MapUtils.emptyMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

@PrototypeComponent ("chatUserImageUploadFormPage")
public
class ChatUserImageUploadFormPage
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
	String introHtml;
	String submitLabel;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			imageUploadToken =
				chatUserImageUploadTokenHelper.findByToken (
					transaction,
					requestContext.requestStringRequired (
						"chatUserImageUploadToken"));

			chatUser =
				imageUploadToken.getChatUser ();

			titleText =
				chatSendLogic.renderTemplate (
					transaction,
					chatUser,
					"web",
					"image_upload_form_title",
					emptyMap ());

			introHtml =
				chatSendLogic.renderTemplate (
					transaction,
					chatUser,
					"web",
					"image_upload_form_intro",
					emptyMap ());

			submitLabel =
				chatSendLogic.renderTemplate (
					transaction,
					chatUser,
					"web",
					"image_upload_form_submit",
					emptyMap ());

		}

	}

	@Override
	protected
	void goHeaders (
			@NonNull Transaction parentTransaction) {

		requestContext.addHeader (
			"Content-Type",
			"text/html; charset=utf-8");

	}

	@Override
	protected
	void goContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

		) {

			formatWriter.writeLineFormat (
				"<!DOCTYPE html>");

			formatWriter.writeLineFormatIncreaseIndent (
				"<html>");

			goHead (
				transaction);

			goBody (
				transaction);

			formatWriter.writeLineFormatDecreaseIndent (
				"</html>");

		}

	}

	protected
	void goHead (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
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
			introHtml);

		formatWriter.writeLineFormatIncreaseIndent (
			"<form",
			" method=\"post\"",
			" enctype=\"multipart/form-data\"",
			">");

		formatWriter.writeLineFormat (
			"<p><input",
			" type=\"file\"",
			" name=\"file\"",
			"></p>");

		formatWriter.writeLineFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"%h\"",
			submitLabel,
			"></p>");

		formatWriter.writeLineFormatDecreaseIndent (
			"</form>");

		formatWriter.writeLineFormatDecreaseIndent (
			"</body>");

	}

}
