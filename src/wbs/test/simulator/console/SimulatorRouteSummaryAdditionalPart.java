package wbs.test.simulator.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("simulatorRouteSummaryAdditionalPart")
public
class SimulatorRouteSummaryAdditionalPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<h2>Simulator route information</h2>\n");

		printFormat (
			"<p>This is a simulator route and its messages will be delivered ",
			"to the simulator message queue.</p>\n");

	}

}
