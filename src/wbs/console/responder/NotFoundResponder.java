package wbs.console.responder;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormatError;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("notFoundResponder")
public
class NotFoundResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		htmlHeadingOneWrite (
			"Page not found");

		htmlParagraphWriteFormatError (
			"Page not found");

		htmlParagraphWriteFormat (
			"The requested page could not be found:");

		htmlParagraphWriteHtml (
			stringFormat (
				"<code>%h</code>",
				requestContext.requestUri ()));

	}

}
