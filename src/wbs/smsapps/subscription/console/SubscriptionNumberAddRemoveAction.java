package wbs.smsapps.subscription.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("subscriptionNumberAddRemoveAction")
public
class SubscriptionNumberAddRemoveAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SubscriptionConsoleHelper subscriptionHelper;

	@SingletonDependency
	@Named
	ConsoleModule subscriptionNumberConsoleModule;

	@SingletonDependency
	SubscriptionNumberConsoleHelper subscriptionNumberHelper;

	@SingletonDependency
	SubscriptionSubConsoleHelper subscriptionSubHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"subscriptionNumberAddRemoveResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			SubscriptionRec subscription =
				subscriptionHelper.findFromContextRequired (
					transaction);

			// process form

			FormFieldSet <SubscriptionNumberAddRemoveForm>
				addRemoveFormFieldSet =
					subscriptionNumberConsoleModule.formFieldSetRequired (
						"addRemoveForm",
						SubscriptionNumberAddRemoveForm.class);

			SubscriptionNumberAddRemoveForm addRemoveForm =
				new SubscriptionNumberAddRemoveForm ();

			formFieldLogic.update (
				transaction,
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
						transaction,
						numberString);

				numbers.add (
					number);

			}

			// add numbers

			if (
				optionalIsPresent (
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
							transaction,
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
							transaction,
							subscriptionSubHelper.createInstance ()

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
							userConsoleLogic.userRequired (
								transaction))

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

					requestContext.addNoticeFormat (
						"%s numbers added",
						integerToDecimalString (
							numAdded));

				}

				if (numAlreadyAdded > 0) {

					requestContext.addWarningFormat (
						"%s numbers already added",
						integerToDecimalString (
							numAlreadyAdded));

				}

				return null;

			}

			// remove numbers

			if (
				optionalIsPresent (
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
							transaction,
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
							userConsoleLogic.userRequired (
								transaction))

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
							+ activeSubscriptionAffiliate.getNumSubscribers ()
							- 1);

					activeSubscriptionList

						.setNumSubscribers (
							+ activeSubscriptionList.getNumSubscribers ()
							- 1);

					numRemoved ++;

				}

				// commit etc

				transaction.commit ();

				if (
					moreThanZero (
						numRemoved)
				) {

					requestContext.addNoticeFormat (
						"%s numbers removed",
						integerToDecimalString (
							numRemoved));

				}

				if (
					moreThanZero (
						numAlreadyRemoved)
				) {

					requestContext.addWarningFormat (
						"%s numbers already removed",
						integerToDecimalString (
							numAlreadyRemoved));

				}

				return null;

			}

			// error

			throw new RuntimeException ();

		}

	}

}
