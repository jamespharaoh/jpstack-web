package wbs.smsapps.subscription.console;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;

@PrototypeComponent ("subscriptionNumberOverviewPart")
public
class SubscriptionNumberOverviewPart
	extends AbstractPagePart {

	// dependencies

	@Inject
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

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Subscribers</th>\n",
			"<td>%h</td>\n",
			subscription.getNumSubscribers (),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
