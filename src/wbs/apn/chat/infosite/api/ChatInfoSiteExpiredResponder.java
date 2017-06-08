package wbs.apn.chat.infosite.api;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

@PrototypeComponent ("chatInfoSiteExpiredResponder")
public
class ChatInfoSiteExpiredResponder
	extends PrintResponder {

	// dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

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

			requestContext.contentType (
				"text/html",
				"utf-8");

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

			htmlParagraphWrite (
				"This message is no longer valid.");

		}

	}

}