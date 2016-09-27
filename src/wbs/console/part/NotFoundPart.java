package wbs.console.part;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteHtml;

import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("notFoundPart")
public
class NotFoundPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

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
