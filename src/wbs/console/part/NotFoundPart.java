package wbs.console.part;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("notFoundPart")
public
class NotFoundPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlParagraphWrite (
			"Page not found",
			htmlClassAttribute (
				"error"));

		htmlParagraphWrite (
			"The requested page could not be found:");

		htmlParagraphWriteHtml (
			stringFormat (
				"<code>%h</code>",
				requestContext.requestUri ()));

	}

}
