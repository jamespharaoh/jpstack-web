package wbs.smsapps.subscription.console;

import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@PrototypeComponent ("subscriptionAdminSearchNumbersResponder")
public
class SubscriptionAdminSearchNumbersResponder
	extends ConsoleResponder {

	@Inject
	ConsoleRequestContext requestContext;

	List<SubscriptionSubRec> subscriptionSubs;

	@Override
	@SuppressWarnings ("unchecked")
	public
	void prepare () {

		subscriptionSubs =
			(List<SubscriptionSubRec>)
			requestContext.request ("subscription_search_results");

	}

	@Override
	public
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/plain");

		requestContext.setHeader (
			"Content-Disposition",
			"attachment; filename=numbers.txt");

	}

	@Override
	public
	void render () {

		PrintWriter out =
			requestContext.writer ();

		for (
			SubscriptionSubRec subscriptionSub
				: subscriptionSubs
		) {

			SubscriptionNumberRec subscriptionNumber =
				subscriptionSub.getSubscriptionNumber ();

			NumberRec number =
				subscriptionNumber.getNumber ();

			out.println (
				number.getNumber ());

		}

	}

}
