package wbs.test.simulator.console;

import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("simulatorRouteSummaryAdditionalPart")
public
class SimulatorRouteSummaryAdditionalPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		htmlHeadingTwoWrite (
			"Simulator route information");

		htmlParagraphWriteFormat (
			"This is a simulator route and its messages will be delivered to ",
			"the simulator message queue.");

	}

}
