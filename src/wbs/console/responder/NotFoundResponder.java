package wbs.console.responder;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormatError;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("notFoundResponder")
public
class NotFoundResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContents");

		) {

			htmlHeadingOneWrite (
				formatWriter,
				"Page not found");

			htmlParagraphWriteFormatError (
				formatWriter,
				"Page not found");

			htmlParagraphWriteFormat (
				formatWriter,
				"The requested page could not be found:");

			htmlParagraphWriteHtml (
				formatWriter,
				stringFormat (
					"<code>%h</code>",
					requestContext.requestUri ()));

		}

	}

}
