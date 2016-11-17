package wbs.test.simulator.console;

import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("simulatorRouteSummaryAdditionalPart")
public
class SimulatorRouteSummaryAdditionalPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlHeadingTwoWrite (
			"Simulator route information");

		htmlParagraphWriteFormat (
			"This is a simulator route and its messages will be delivered to ",
			"the simulator message queue.");

	}

}
