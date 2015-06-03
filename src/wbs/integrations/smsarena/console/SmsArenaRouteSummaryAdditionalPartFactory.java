package wbs.integrations.smsarena.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.part.PagePart;
import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("smsArenaRouteSummaryAdditionalPartFactory")
public class SmsArenaRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	// dependencies

	@Inject
	Provider<SmsArenaRouteSummaryAdditionalPart>
		smsArenaRouteSummaryAdditionalPart;

	// details

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"sms-arena"
		};

	}

	// implementation

	@Override
	public
	PagePart getPagePart (
			String senderCode) {

		return smsArenaRouteSummaryAdditionalPart.get ();

	}

}
