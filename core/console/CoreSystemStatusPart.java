package wbs.platform.core.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("coreSystemStatusPart")
public
class CoreSystemStatusPart
	extends AbstractPagePart {

	// implementation

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlParagraphWrite (
			"TODO");

	}

}
