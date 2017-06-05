package wbs.apn.chat.user.image.api;

import static wbs.utils.collection.MapUtils.emptyMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

@PrototypeComponent ("chatUserImageUploadExpiredPage")
public
class ChatUserImageUploadExpiredPage
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
					"image_upload_expired_title",
					emptyMap ());

			bodyHtml =
				chatSendLogic.renderTemplate (
					transaction,
					chatUser,
					"web",
					"image_upload_expired_body",
					emptyMap ());

		}

	}

	@Override
	protected
	void goHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeaders");

		) {

			requestContext.addHeader (
				"Content-Type",
				"text/html; charset=utf-8");

		}

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goBody");

		) {

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

}
