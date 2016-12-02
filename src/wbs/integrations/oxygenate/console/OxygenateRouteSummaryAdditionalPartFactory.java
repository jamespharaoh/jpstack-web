package wbs.integrations.oxygenate.console;

import javax.inject.Provider;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("oxygenateRouteSummaryAdditionalPartFactory")
public
class OxygenateRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// prototype dependencies

	@PrototypeDependency
	Provider <OxygenateRouteSummaryAdditionalPart>
	oxygen8RouteSummaryAdditionalPartProvider;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"oxygen8"
		};

	}

	// implementation

	@Override
	public
	PagePart getPagePart (
			String senderCode) {

		return oxygen8RouteSummaryAdditionalPartProvider.get ();

	}

}
