package wbs.smsapps.subscription.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		subscription =
			subscriptionHelper.findRequired (
				requestContext.stuffInteger (
					"subscriptionId"));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Subscribers",
			integerToDecimalString (
				subscription.getNumSubscribers ()));

		htmlTableClose ();

	}

}
