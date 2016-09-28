package wbs.console.responder;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormatError;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteHtml;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
	void renderHtmlBodyContents () {

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
