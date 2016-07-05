package wbs.smsapps.alerts.console;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.pluralise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import com.google.common.collect.ImmutableSet;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

	// dependencies

	@Inject
	AlertsNumberConsoleHelper alertsNumberHelper;

	@Inject
	AlertsSettingsConsoleHelper alertsSettingsHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberConsoleHelper numberHelper;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
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
	Responder goReal ()
		throws ServletException {

		if (! requestContext.canContext ("alertsSettings.manage")) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		List<String> notices =
			new ArrayList<String> ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.findOrNull (
				requestContext.stuffInt (
					"alertsSettingsId"));

		// add/delete

		int updatedNames = 0;
		int updatedNumbers = 0;
		int numEnabled = 0;
		int numDisabled = 0;

		for (
			Iterator<AlertsNumberRec> alertsNumberIterator =
				alertsSettings.getAlertsNumbers ().iterator ();
			alertsNumberIterator.hasNext ();
			doNothing ()
		) {

			AlertsNumberRec alertsNumber =
				alertsNumberIterator.next ();

			// delete

			if (
				isNotNull (
					requestContext.getForm (
						stringFormat (
							"delete_%d",
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
				requestContext.getForm (
					stringFormat (
						"name_%d",
						alertsNumber.getId ()),
					"");

			if (newName == null)
				continue;

			if (
				! equal (
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
				requestContext.getForm (
					stringFormat (
						"number_%s",
						alertsNumber.getId ()));

			if (
				notEqual (
					alertsNumber.getNumber ().getNumber (),
					newNumber)
			) {

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

			boolean newEnabled =
				Boolean.parseBoolean (
					requestContext.getForm (
						stringFormat (
							"enabled_%s",
							alertsNumber.getId ())));

			if (
				notEqual (
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
			isNotNull (
				requestContext.getForm (
					"add_new"))
		) {

			NumberRec numberRec =
				numberHelper.findOrCreate (
					requestContext.getForm (
						"number_new"));

			AlertsNumberRec alertsNumber =
				alertsNumberHelper.createInstance ()

				.setAlertsSettings (
					alertsSettings)

				.setName (
					requestContext.getForm (
						"name_new",
						""))

				.setNumber (
					numberRec);

			boolean newEnabled =
				Boolean.parseBoolean (
					requestContext.getForm (
						"enabled_new",
						""));

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
			isNotEmpty (
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

		return null;

	}

}
