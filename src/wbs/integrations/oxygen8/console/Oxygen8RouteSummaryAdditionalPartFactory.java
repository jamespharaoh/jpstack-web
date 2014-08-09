package wbs.integrations.oxygen8.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.part.PagePart;
import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("oxygen8RouteSummaryAdditionalPartFactory")
public
class Oxygen8RouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// dependencies

	@Inject
	Provider<Oxygen8RouteSummaryAdditionalPart>
		oxygen8RouteSummaryAdditionalPart;

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

		return oxygen8RouteSummaryAdditionalPart.get ();

	}

}
