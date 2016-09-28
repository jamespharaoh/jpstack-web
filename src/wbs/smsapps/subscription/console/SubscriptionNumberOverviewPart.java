package wbs.smsapps.subscription.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;

@PrototypeComponent ("subscriptionNumberOverviewPart")
public
class SubscriptionNumberOverviewPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	SubscriptionObjectHelper subscriptionHelper;

	// state

	SubscriptionRec subscription;

	// implementation

	@Override
	public
	void prepare () {

		subscription =
			subscriptionHelper.findRequired (
				requestContext.stuffInteger (
					"subscriptionId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Subscribers",
			integerToDecimalString (
				subscription.getNumSubscribers ()));

		htmlTableClose ();

	}

}
