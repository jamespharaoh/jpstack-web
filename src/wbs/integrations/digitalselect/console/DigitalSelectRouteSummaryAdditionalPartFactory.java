package wbs.integrations.digitalselect.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("digitalSelectRouteSummaryAdditionalPartFactory")
public
class DigitalSelectRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	@Inject
	Provider<DigitalSelectRouteSummaryAdditionalPart>
		digitalSelectRouteSummaryAdditionalPart;

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"digital_select"
		};

	}

	@Override
	public
	PagePart getPagePart (
			String senderCode) {

		return digitalSelectRouteSummaryAdditionalPart.get ();

	}

}
