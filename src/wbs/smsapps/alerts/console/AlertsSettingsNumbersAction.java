package wbs.smsapps.alerts.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.pluralise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsObjectHelper;
import wbs.smsapps.alerts.model.AlertsSettingsRec;

@PrototypeComponent ("alertsSettingsNumbersAction")
public
class AlertsSettingsNumbersAction
	extends ConsoleAction {

	@Inject
	AlertsSettingsObjectHelper alertsSettingsHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberLogic numberLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("alertsSettingsNumbersResponder");
	}

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		if (! requestContext.canContext ("alertsSettings.manage")) {
			requestContext.addError ("Access denied");
			return null;
		}

		List<String> notices =
			new ArrayList<String> ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.find (
				requestContext.stuffInt ("alertsSettingsId"));

		// add/delete

		int updatedNames = 0;
		int updatedNumbers = 0;
		int numEnabled = 0;
		int numDisabled = 0;

		for (AlertsNumberRec alertsNumber
				: alertsSettings.getAlertsNumbers ()) {

			// delete

			if (
				requestContext.getForm (
					stringFormat (
						"delete_%d",
						alertsNumber.getId ())
				) != null
			) {

				objectManager.remove (
					alertsNumber);

				eventLogic.createEvent (
					"alerts_number_deleted",
					myUser,
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
						alertsNumber.getId ()));

			if (newName == null)
				continue;

			if (! equal (
					alertsNumber.getName (),
					newName)) {

				alertsNumber.setName (newName);

				updatedNames ++;

				eventLogic.createEvent (
					"alerts_number_updated",
					myUser,
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

			if (! equal (
					alertsNumber.getNumber ().getNumber (),
					newNumber)) {

				NumberRec numberRec =
					numberLogic.findOrCreateNumber (newNumber);

				alertsNumber.setNumber (numberRec);

				updatedNumbers ++;

				eventLogic.createEvent (
					"alerts_number_updated",
					myUser,
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

			if (alertsNumber.getEnabled () != newEnabled) {

				alertsNumber.setEnabled (newEnabled);

				if (newEnabled) numEnabled ++;
				else numDisabled ++;

				eventLogic.createEvent (
					"alerts_number_updated",
					myUser,
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
					pluralise (updatedNames, "name")));

		}

		if (updatedNumbers > 0) {

			notices.add (
				stringFormat (
					"Updated %s",
					pluralise (updatedNumbers, "number")));

		}

		if (numEnabled > 0) {

			notices.add (
				stringFormat (
					"Enabled %s",
					pluralise (numEnabled, "number")));

		}

		if (numDisabled > 0) {

			notices.add (
				stringFormat (
					"Disabled %s",
					pluralise (numDisabled, "number")));

		}

		// add

		if (requestContext.getForm ("add_new") != null) {

			NumberRec numberRec =
				numberLogic.findOrCreateNumber (
					requestContext.getForm ("number_new"));

			AlertsNumberRec alertsNumber =
				new AlertsNumberRec ()
					.setAlertsSettings (alertsSettings)
					.setName (requestContext.getForm ("name_new"))
					.setNumber (numberRec);

			boolean newEnabled =
				Boolean.parseBoolean (
					requestContext.getForm ("enabled_new"));

			alertsNumber.setEnabled (newEnabled);

			objectManager.insert (alertsNumber);

			eventLogic.createEvent (
				"alerts_number_created",
				myUser,
				alertsNumber.getId (),
				alertsSettings);

			eventLogic.createEvent (
				"alerts_number_updated",
				myUser,
				"name",
				alertsNumber.getId (),
				alertsSettings,
				alertsNumber.getName ());

			eventLogic.createEvent (
				"alerts_number_updated",
				myUser,
				"number",
				alertsNumber.getId (),
				alertsSettings,
				numberRec);

			eventLogic.createEvent (
				"alerts_number_updated",
				myUser,
				"enabled",
				alertsNumber.getId (),
				alertsSettings,
				alertsNumber.getEnabled ());

			notices.add ("Added new number");

			requestContext.hideFormData (
				"name_new",
				"number_new",
				"enabled_new");

		}

		// finish up

		transaction.commit ();

		for (String notice : notices)
			requestContext.addNotice (notice);

		return null;

	}

}
