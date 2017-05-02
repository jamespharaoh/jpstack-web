package wbs.test.simulator.console;

import javax.inject.Provider;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("simulatorRouteSummaryAdditionalPartFactory")
public
class SimulatorRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// prototype dependencies

	@PrototypeDependency
	Provider <SimulatorRouteSummaryAdditionalPart>
		simulatorRouteSummaryAdditionalPartProvider;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"simulator"
		};

	}

	// implementation

	@Override
	public
	PagePart getPagePart (
			String senderCode) {

		return simulatorRouteSummaryAdditionalPartProvider.get ();

	}

}
