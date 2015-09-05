package wbs.smsapps.subscription.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@PrototypeComponent ("subscriptionNumberAddRemoveAction")
public
class SubscriptionNumberAddRemoveAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject @Named
	ConsoleModule subscriptionNumberConsoleModule;

	@Inject
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"subscriptionNumberAddRemoveResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		SubscriptionRec subscription =
			subscriptionHelper.find (
				requestContext.stuffInt (
					"subscriptionId"));

		// process form

		FormFieldSet addRemoveFormFieldSet =
			subscriptionNumberConsoleModule.formFieldSets ().get (
				"addRemoveForm");

		SubscriptionNumberAddRemoveForm addRemoveForm =
			new SubscriptionNumberAddRemoveForm ();

		formFieldLogic.update (
			addRemoveFormFieldSet,
			addRemoveForm);

		// parse numbers

		List<String> numberStrings;

		try {

			numberStrings =
				numberFormatLogic.parseLines (
					addRemoveForm.numberFormat (),
					addRemoveForm.numbers ());

		} catch (WbsNumberFormatException exception) {

			requestContext.addError (
				"Invalid number format");

			return null;

		}

		// lookup numbers

		List<NumberRec> numbers =
			new ArrayList<NumberRec> ();

		for (
			String numberString
				: numberStrings
		) {

			NumberRec number =
				numberHelper.findOrCreate (
					numberString);

			numbers.add (
				number);

		}

		// add numbers

		if (requestContext.parameter ("add") != null) {

			int numAdded = 0;
			int numAlreadyAdded = 0;

			for (
				NumberRec number
					: numbers
			) {

				SubscriptionNumberRec subscriptionNumber =
					subscriptionNumberHelper.findOrCreate (
						subscription,
						number);

				if (subscriptionNumber.getActiveSubscriptionSub () != null) {

					numAlreadyAdded ++;

					continue;

				}

				SubscriptionAffiliateRec newSubscriptionAffiliate =
					ifNull (
						subscriptionNumber.getSubscriptionAffiliate (),
						addRemoveForm.subscriptionAffiliate ());

				SubscriptionListRec newSubscriptionList =
					addRemoveForm.subscriptionList ();

				SubscriptionSubRec subscriptionSub =
					subscriptionSubHelper.insert (
						new SubscriptionSubRec ()

					.setSubscriptionNumber (
						subscriptionNumber)

					.setIndex (
						subscriptionNumber.getNumSubs ())

					.setSubscriptionList (
						newSubscriptionList)

					.setSubscriptionAffiliate (
						newSubscriptionAffiliate)

					.setStarted (
						transaction.now ())

					.setStartedBy (
						myUser)

				);

				subscriptionNumber

					.setActive (
						true)

					.setActiveSubscriptionSub (
						subscriptionSub)

					.setSubscriptionAffiliate (
						newSubscriptionAffiliate)

					.setSubscriptionList (
						newSubscriptionList)

					.setNumSubs (
						subscriptionNumber.getNumSubs () + 1)

					.setFirstJoin (
						ifNull (
							subscriptionNumber.getFirstJoin (),
							transaction.now ()))

					.setLastJoin (
						transaction.now ());

				subscription

					.setNumSubscribers (
						subscription.getNumSubscribers () + 1);

				newSubscriptionAffiliate

					.setNumSubscribers (
						newSubscriptionAffiliate.getNumSubscribers () + 1);

				newSubscriptionList

					.setNumSubscribers (
						newSubscriptionList.getNumSubscribers () + 1);

				numAdded ++;

			}

			// commit etc

			transaction.commit ();

			if (numAdded > 0) {

				requestContext.addNotice (
					stringFormat (
						"%s numbers added",
						numAdded));

			}

			if (numAlreadyAdded > 0) {

				requestContext.addWarning (
					stringFormat (
						"%s numbers already added",
						numAlreadyAdded));

			}

			return null;

		}

		// remove numbers

		if (requestContext.parameter ("remove") != null) {

			int numRemoved = 0;
			int numAlreadyRemoved = 0;

			for (NumberRec number : numbers) {

				SubscriptionNumberRec subscriptionNumber =
					subscriptionNumberHelper.findOrCreate (
						subscription,
						number);

				if (subscriptionNumber.getActiveSubscriptionSub () == null) {

					numAlreadyRemoved ++;

					continue;

				}

				SubscriptionSubRec activeSubscriptionSub =
					subscriptionNumber.getActiveSubscriptionSub ();

				SubscriptionListRec activeSubscriptionList =
					activeSubscriptionSub.getSubscriptionList ();

				SubscriptionAffiliateRec activeSubscriptionAffiliate =
					activeSubscriptionSub.getSubscriptionAffiliate ();

				activeSubscriptionSub

					.setEnded (
						transaction.now ())

					.setEndedBy (
						myUser);

				subscriptionNumber

					.setActive (
						false)

					.setActiveSubscriptionSub (
						null);

				subscription

					.setNumSubscribers (
						subscription.getNumSubscribers () - 1);

				activeSubscriptionAffiliate

					.setNumSubscribers (
						activeSubscriptionAffiliate.getNumSubscribers () - 1);

				activeSubscriptionList

					.setNumSubscribers (
						activeSubscriptionList.getNumSubscribers () - 1);

				numRemoved ++;

			}

			// commit etc

			transaction.commit ();

			if (numRemoved > 0) {

				requestContext.addNotice (
					stringFormat (
						"%s numbers removed",
						numRemoved));

			}

			if (numAlreadyRemoved > 0) {

				requestContext.addWarning (
					stringFormat (
						"%s numbers already removed",
						numAlreadyRemoved));

			}

			return null;

		}

		// error

		throw new RuntimeException ();

	}

}
