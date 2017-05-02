package wbs.integrations.digitalselect.console;

import javax.inject.Provider;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("digitalSelectRouteSummaryAdditionalPartFactory")
public
class DigitalSelectRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// prototype dependencies

	@PrototypeDependency
	Provider <DigitalSelectRouteSummaryAdditionalPart>
		digitalSelectRouteSummaryAdditionalPartProvider;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"digital_select"
		};

	}

	// simplementation

	@Override
	public
	PagePart getPagePart (
			String senderCode) {

		return digitalSelectRouteSummaryAdditionalPartProvider.get ();

	}

}
