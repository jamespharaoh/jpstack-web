package wbs.platform.core.console;

import static wbs.utils.web.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;

import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("coreHomeResponder")
public
class CoreHomeResponder
	extends HtmlResponder {

	@Override
	protected
	void renderHtmlBodyContents () {

		htmlHeadingOneWrite (
			"Home");

		htmlParagraphWrite (
			"Welcome to the SMS console.");

	}

}
