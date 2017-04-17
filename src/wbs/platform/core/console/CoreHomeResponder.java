package wbs.platform.core.console;

import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.console.responder.ConsoleHtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("coreHomeResponder")
public
class CoreHomeResponder
	extends ConsoleHtmlResponder {

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		htmlHeadingOneWrite (
			"Home");

		htmlParagraphWrite (
			"Welcome to the SMS console.");

	}

}
