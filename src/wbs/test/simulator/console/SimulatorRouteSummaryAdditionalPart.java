package wbs.test.simulator.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("simulatorRouteSummaryAdditionalPart")
public
class SimulatorRouteSummaryAdditionalPart
	extends AbstractPagePart {

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h2>Simulator route information</h2>\n");

		printFormat (
			"<p>This is a simulator route and its messages will be delivered ",
			"to the simulator message queue.</p>\n");

	}

}
