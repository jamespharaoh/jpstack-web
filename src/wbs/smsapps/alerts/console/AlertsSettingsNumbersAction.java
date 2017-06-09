package wbs.smsapps.alerts.console;

import static wbs.utils.etc.LogicUtils.booleanNotEqual;
import static wbs.utils.etc.LogicUtils.parseBooleanTrueFalseRequired;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.pluralise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.notice.ConsoleNotices;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsRec;

import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"alertsSettingsNumbersResponder");

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

			if (! requestContext.canContext ("alertsSettings.manage")) {

				requestContext.addError (
					"Access denied");

				return null;

			}

			ConsoleNotices notices =
				new ConsoleNotices ();

			AlertsSettingsRec alertsSettings =
				alertsSettingsHelper.findFromContextRequired (
					transaction);

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
						transaction,
						alertsNumber);

					alertsNumberIterator.remove ();

					eventLogic.createEvent (
						transaction,
						"alerts_number_deleted",
						userConsoleLogic.userRequired (
							transaction),
						alertsNumber.getId (),
						alertsSettings);

					notices.noticeFormat (
						"Deleted 1 number");

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
						transaction,
						"alerts_number_updated",
						userConsoleLogic.userRequired (
							transaction),
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
							transaction,
							newNumber);

					alertsNumber

						.setNumber (
							numberRec);

					updatedNumbers ++;

					eventLogic.createEvent (
						transaction,
						"alerts_number_updated",
						userConsoleLogic.userRequired (
							transaction),
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
						transaction,
						"alerts_number_updated",
						userConsoleLogic.userRequired (
							transaction),
						"enabled",
						alertsNumber.getId (),
						alertsSettings,
						newEnabled);

				}

			}

			if (updatedNames > 0) {

				notices.noticeFormat (
					"Updated %s",
					pluralise (
						updatedNames,
						"name"));

			}

			if (updatedNumbers > 0) {

				notices.noticeFormat (
					"Updated %s",
					pluralise (
						updatedNumbers,
						"number"));

			}

			if (numEnabled > 0) {

				notices.noticeFormat (
					"Enabled %s",
					pluralise (
						numEnabled,
						"number"));

			}

			if (numDisabled > 0) {

				notices.noticeFormat (
					"Disabled %s",
					pluralise (
						numDisabled,
						"number"));

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
						transaction,
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
					transaction,
					alertsNumber);

				eventLogic.createEvent (
					transaction,
					"alerts_number_created",
					userConsoleLogic.userRequired (
						transaction),
					alertsNumber.getId (),
					alertsSettings);

				eventLogic.createEvent (
					transaction,
					"alerts_number_updated",
					userConsoleLogic.userRequired (
						transaction),
					"name",
					alertsNumber.getId (),
					alertsSettings,
					alertsNumber.getName ());

				eventLogic.createEvent (
					transaction,
					"alerts_number_updated",
					userConsoleLogic.userRequired (
						transaction),
					"number",
					alertsNumber.getId (),
					alertsSettings,
					numberRec);

				eventLogic.createEvent (
					transaction,
					"alerts_number_updated",
					userConsoleLogic.userRequired (
						transaction),
					"enabled",
					alertsNumber.getId (),
					alertsSettings,
					alertsNumber.getEnabled ());

				notices.noticeFormat (
					"Added new number");

				requestContext.hideFormData (
					"name_new",
					"number_new",
					"enabled_new");

			}

			// finish up

			transaction.commit ();

			notices.noticeIfEmptyFormat (
				"No changes to save");

			requestContext.setEmptyFormData ();

			return null;

		}

	}

}
