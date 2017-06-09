package wbs.apn.chat.infosite.api;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.BufferedTextResponder;

@PrototypeComponent ("chatInfoSiteMessageSentResponder")
public
class ChatInfoSiteMessageSentResponder
	extends BufferedTextResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	protected
	void headers (
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
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

		) {

			htmlParagraphWrite (
				formatWriter,
				"Your message has been sent.");

		}

	}

}
