package wbs.smsapps.autoresponder.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("autoResponderSettingsTemplatesPart")
public
class AutoResponderSettingsTemplatesPart
	extends AbstractPagePart {

	// public implementation

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlParagraphWrite (
			"This feature is under development and will be available soon.");

	}

}
