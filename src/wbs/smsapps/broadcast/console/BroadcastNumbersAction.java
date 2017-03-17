package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.List;

import javax.servlet.ServletException;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.list.console.NumberListNumberConsoleHelper;

import wbs.smsapps.broadcast.logic.BroadcastLogic;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("broadcastNumbersAction")
public
class BroadcastNumbersAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	BroadcastConsoleHelper broadcastHelper;

	@SingletonDependency
	BroadcastLogic broadcastLogic;

	@SingletonDependency
	BroadcastNumberConsoleHelper broadcastNumberHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	NumberListNumberConsoleHelper numberListNumberHelper;

	@SingletonDependency
	NumberObjectHelper numberHelper;

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
	protected
	Responder backupResponder () {
		return responder ("broadcastNumbersResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger)
		throws ServletException {

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"BroadcastNumbersAction.goReal ()",
					this);

		) {

			BroadcastRec broadcast =
				broadcastHelper.findFromContextRequired ();

			BroadcastConfigRec broadcastConfig =
				broadcast.getBroadcastConfig ();

			// parse numbers

			List <String> numbers;

			try {

				numbers =
					numberFormatLogic.parseLines (
						broadcastConfig.getNumberFormat (),
						requestContext.parameterRequired (
							"numbers"));

			} catch (WbsNumberFormatException exception) {

				requestContext.addNotice (
					"Invalid number format");

				return null;

			}

			// check permissions

			if (
				! privChecker.canRecursive (
					broadcastConfig,
					"manage")
			) {

				throw new RuntimeException ();

			}

			// check state

			if (broadcast.getState () != BroadcastState.unsent) {

				requestContext.addErrorFormat (
					"Can't modify numbers for broadcast in %s state",
					enumNameSpaces (
						broadcast.getState ()));

				return null;

			}

			// add numbers

			BroadcastLogic.AddResult addResult;

			if (
				optionalIsPresent (
					requestContext.parameter (
						"add"))
			) {

				addResult =
					broadcastLogic.addNumbers (
						broadcast,
						numbers,
						userConsoleLogic.userRequired ());

			} else {

				addResult =
					new BroadcastLogic.AddResult ();

			}

			// remove numbers

			int numAlreadyRemoved = 0;
			int numRemoved = 0;

			if (
				optionalIsPresent (
					requestContext.parameter (
						"remove"))
			) {

				for (
					String numberString
						: numbers
				) {

					NumberRec numberRecord =
						numberHelper.findOrCreate (
							numberString);

					BroadcastNumberRec broadcastNumber =
						broadcastNumberHelper.find (
							broadcast,
							numberRecord);

					if (broadcastNumber == null) {

						numAlreadyRemoved ++;

						continue;

					}

					switch (broadcastNumber.getState ()) {

					case removed:

						numAlreadyRemoved ++;

						break;

					case accepted:

						broadcastNumber

							.setState (
								BroadcastNumberState.removed)

							.setRemovedByUser (
								userConsoleLogic.userRequired ());

						broadcast

							.setNumAccepted (
								broadcast.getNumAccepted () - 1)

							.setNumRemoved (
								broadcast.getNumRemoved () + 1)

							.setNumTotal (
								broadcast.getNumTotal () - 1);

						numRemoved ++;

						break;

					case rejected:

						broadcastNumber

							.setState (
								BroadcastNumberState.removed)

							.setRemovedByUser (
								userConsoleLogic.userRequired ());

						broadcast

							.setNumRejected (
								broadcast.getNumRejected () - 1)

							.setNumRemoved (
								broadcast.getNumRemoved () + 1);

						numRemoved ++;

						break;

					case sent:

						shouldNeverHappen ();

					}

				}

			}

			// events

			if (addResult.numAdded () > 0) {

				eventLogic.createEvent (
					"broadcast_numbers_added",
					userConsoleLogic.userRequired (),
					addResult.numAdded (),
					broadcast);

			}

			if (numRemoved > 0) {

				eventLogic.createEvent (
					"broadcast_numbers_removed",
					userConsoleLogic.userRequired (),
					numRemoved,
					broadcast);

			}

			// commit

			transaction.commit ();

			// report changes

			if (addResult.numAlreadyAdded () > 0) {

				requestContext.addWarningFormat (
					"%s numbers already added",
					integerToDecimalString (
						addResult.numAlreadyAdded ()));

			}

			if (addResult.numAlreadyRejected () > 0) {

				requestContext.addWarningFormat (
					"%s numbers already rejected",
					integerToDecimalString (
						addResult.numAlreadyRejected ()));

			}

			if (addResult.numAdded () > 0) {

				requestContext.addNoticeFormat (
					"%s numbers added",
					integerToDecimalString (
						addResult.numAdded ()));

			}

			if (addResult.numRejected () > 0) {

				requestContext.addWarningFormat (
					"%s numbers rejected",
					integerToDecimalString (
						addResult.numRejected ()));

			}

			if (numAlreadyRemoved > 0) {

				requestContext.addWarningFormat (
					"%s numbers already removed or never added",
					integerToDecimalString (
						numAlreadyRemoved));

			}

			if (numRemoved > 0) {

				requestContext.addNoticeFormat (
					"%s numbers removed",
					integerToDecimalString (
						numRemoved));

			}

			return null;

		}

	}

}
