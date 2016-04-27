package wbs.smsapps.subscription.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.moreThanZero;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;

import com.google.common.collect.ImmutableMap;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
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
	UserConsoleLogic userConsoleLogic;

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
			requestContext,
			addRemoveFormFieldSet,
			addRemoveForm,
			ImmutableMap.of (),
			"update");

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

		if (
			isNotNull (
				requestContext.parameter (
					"add"))
		) {

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

				if (
					isNotNull (
						subscriptionNumber.getActiveSubscriptionSub ())
				) {

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
						subscriptionSubHelper.createInstance ()

					.setSubscriptionNumber (
						subscriptionNumber)

					.setIndex (
						(int) (long)
						subscriptionNumber.getNumSubs ())

					.setSubscriptionList (
						newSubscriptionList)

					.setSubscriptionAffiliate (
						newSubscriptionAffiliate)

					.setStarted (
						transaction.now ())

					.setStartedBy (
						userConsoleLogic.userRequired ())

					.setActive (
						true)

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

		if (
			isNotNull (
				requestContext.parameter (
					"remove"))
		) {

			int numRemoved = 0;
			int numAlreadyRemoved = 0;

			for (
				NumberRec number
					: numbers
			) {

				SubscriptionNumberRec subscriptionNumber =
					subscriptionNumberHelper.findOrCreate (
						subscription,
						number);

				if (
					isNull (
						subscriptionNumber.getActiveSubscriptionSub ())
				) {

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
						userConsoleLogic.userRequired ())

					.setActive (
						false);

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

			if (
				moreThanZero (
					numRemoved)
			) {

				requestContext.addNotice (
					stringFormat (
						"%s numbers removed",
						numRemoved));

			}

			if (
				moreThanZero (
					numAlreadyRemoved)
			) {

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
