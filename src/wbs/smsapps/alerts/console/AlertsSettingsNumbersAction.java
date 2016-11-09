package wbs.smsapps.alerts.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.LogicUtils.booleanNotEqual;
import static wbs.utils.etc.LogicUtils.parseBooleanTrueFalseRequired;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.pluralise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.google.common.collect.ImmutableSet;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsRec;

@PrototypeComponent ("alertsSettingsNumbersAction")
public
class AlertsSettingsNumbersAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	AlertsNumberConsoleHelper alertsNumberHelper;

	@SingletonDependency
	AlertsSettingsConsoleHelper alertsSettingsHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	NumberConsoleHelper numberHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"alertsSettingsNumbersResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger)
		throws ServletException {

		if (! requestContext.canContext ("alertsSettings.manage")) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		List <String> notices =
			new ArrayList<> ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"AlertsSettingsNumbersAction.goReal ()",
				this);

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.findRequired (
				requestContext.stuffInteger (
					"alertsSettingsId"));

		// add/delete

		int updatedNames = 0;
		int updatedNumbers = 0;
		int numEnabled = 0;
		int numDisabled = 0;

		Set <String> numbersSeen =
			new HashSet<> ();

		for (
			Iterator <AlertsNumberRec> alertsNumberIterator =
				alertsSettings.getAlertsNumbers ().iterator ();
			alertsNumberIterator.hasNext ();
			doNothing ()
		) {

			AlertsNumberRec alertsNumber =
				alertsNumberIterator.next ();

			// delete

			if (
				requestContext.formIsPresent (
					stringFormat (
						"delete_%s",
						integerToDecimalString (
							alertsNumber.getId ())))
			) {

				alertsNumberHelper.remove (
					alertsNumber);

				alertsNumberIterator.remove ();

				eventLogic.createEvent (
					"alerts_number_deleted",
					userConsoleLogic.userRequired (),
					alertsNumber.getId (),
					alertsSettings);

				notices.add (
					stringFormat (
						"Deleted 1 number"));

				continue;

			}

			// update

			String newName =
				requestContext.formOrEmptyString (
					stringFormat (
						"name_%s",
						integerToDecimalString (
							alertsNumber.getId ())));

			if (newName == null)
				continue;

			if (
				stringNotEqualSafe (
					alertsNumber.getName (),
					newName)
			) {

				alertsNumber

					.setName (
						newName);

				updatedNames ++;

				eventLogic.createEvent (
					"alerts_number_updated",
					userConsoleLogic.userRequired (),
					"name",
					alertsNumber.getId (),
					alertsSettings,
					newName);

			}

			String newNumber =
				requestContext.formRequired (
					stringFormat (
						"number_%s",
						integerToDecimalString (
							alertsNumber.getId ())));

			if (
				stringNotEqualSafe (
					alertsNumber.getNumber ().getNumber (),
					newNumber)
			) {

				if (
					contains (
						numbersSeen,
						newNumber)
				) {

					requestContext.addError (
						stringFormat (
							"Duplicate number: %s",
							newNumber));

					return null;

				}

				NumberRec numberRec =
					numberHelper.findOrCreate (
						newNumber);

				alertsNumber

					.setNumber (
						numberRec);

				updatedNumbers ++;

				eventLogic.createEvent (
					"alerts_number_updated",
					userConsoleLogic.userRequired (),
					"number",
					alertsNumber.getId (),
					alertsSettings,
					numberRec);

			}

			numbersSeen.add (
				newNumber);

			boolean newEnabled =
				parseBooleanTrueFalseRequired (
					requestContext.formRequired (
						stringFormat (
							"enabled_%s",
							integerToDecimalString (
								alertsNumber.getId ()))));

			if (
				booleanNotEqual (
					alertsNumber.getEnabled (),
					newEnabled)
			) {

				alertsNumber

					.setEnabled (
						newEnabled);

				if (newEnabled) {

					numEnabled ++;

				} else {

					numDisabled ++;

				}

				eventLogic.createEvent (
					"alerts_number_updated",
					userConsoleLogic.userRequired (),
					"enabled",
					alertsNumber.getId (),
					alertsSettings,
					newEnabled);

			}

		}

		if (updatedNames > 0) {

			notices.add (
				stringFormat (
					"Updated %s",
					pluralise (
						updatedNames,
						"name")));

		}

		if (updatedNumbers > 0) {

			notices.add (
				stringFormat (
					"Updated %s",
					pluralise (
						updatedNumbers,
						"number")));

		}

		if (numEnabled > 0) {

			notices.add (
				stringFormat (
					"Enabled %s",
					pluralise (
						numEnabled,
						"number")));

		}

		if (numDisabled > 0) {

			notices.add (
				stringFormat (
					"Disabled %s",
					pluralise (
						numDisabled,
						"number")));

		}

		// add

		if (
			requestContext.formIsPresent (
				"add_new")
		) {

			String newNumber =
				requestContext.formRequired (
					"number_new");

			if (
				contains (
					numbersSeen,
					newNumber)
			) {

				requestContext.addError (
					stringFormat (
						"Duplicate number: %s",
						newNumber));

				return null;

			}

			NumberRec numberRec =
				numberHelper.findOrCreate (
					newNumber);

			AlertsNumberRec alertsNumber =
				alertsNumberHelper.createInstance ()

				.setAlertsSettings (
					alertsSettings)

				.setName (
					requestContext.formOrEmptyString (
						"name_new"))

				.setNumber (
					numberRec);

			boolean newEnabled =
				Boolean.parseBoolean (
					requestContext.formOrEmptyString (
						"enabled_new"));

			alertsNumber

				.setEnabled (
					newEnabled);

			alertsNumberHelper.insert (
				alertsNumber);

			eventLogic.createEvent (
				"alerts_number_created",
				userConsoleLogic.userRequired (),
				alertsNumber.getId (),
				alertsSettings);

			eventLogic.createEvent (
				"alerts_number_updated",
				userConsoleLogic.userRequired (),
				"name",
				alertsNumber.getId (),
				alertsSettings,
				alertsNumber.getName ());

			eventLogic.createEvent (
				"alerts_number_updated",
				userConsoleLogic.userRequired (),
				"number",
				alertsNumber.getId (),
				alertsSettings,
				numberRec);

			eventLogic.createEvent (
				"alerts_number_updated",
				userConsoleLogic.userRequired (),
				"enabled",
				alertsNumber.getId (),
				alertsSettings,
				alertsNumber.getEnabled ());

			notices.add (
				"Added new number");

			requestContext.hideFormData (
				ImmutableSet.<String>of (
					"name_new",
					"number_new",
					"enabled_new"));

		}

		// finish up

		transaction.commit ();

		if (
			collectionIsNotEmpty (
				notices)
		) {

			for (
				String notice
					: notices
			) {

				requestContext.addNotice (
					notice);

			}

		} else {

			requestContext.addWarning (
				"No changes to save");

		}

		requestContext.setEmptyFormData ();

		return null;

	}

}
