package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

@PrototypeComponent ("manualResponderRequestPendingNumberNoteUpdateAction")
public
class ManualResponderRequestPendingNumberNoteUpdateAction
	extends ConsoleAction {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ManualResponderConsoleHelper manualResponderHelper;

	@Inject
	ManualResponderNumberConsoleHelper manualResponderNumberHelper;

	@Inject
	NumberConsoleHelper numberHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject Provider<TextResponder> textResponder;

	static
	Pattern idPattern =
		Pattern.compile ("manualResponderNumberNote([0-9]+)");

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal () {

		// get params

		Matcher idMatcher =
			idPattern.matcher (
				requestContext.parameter ("id"));

		if (! idMatcher.matches ())
			throw new RuntimeException (
				"Invalid id in post");

		int numberId =
			Integer.parseInt (idMatcher.group (1));

		String valueParam =
			requestContext.parameter ("value").trim ();

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ManualResponderRec manualResponder =
			manualResponderHelper.find (
				requestContext.stuffInt ("manualResponderId"));

		NumberRec number =
			numberHelper.find (
				numberId);

		// find or create number

		ManualResponderNumberRec manualResponderNumber =
			manualResponderNumberHelper.find (
				manualResponder,
				number);

		if (manualResponderNumber == null) {

			manualResponderNumber =
				manualResponderNumberHelper.insert (
					manualResponderNumberHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setNumber (
					number)

			);

		}

		// find old and new value

		TextRec oldValue =
			manualResponderNumber.getNotesText ();

		TextRec newValue =
			valueParam.isEmpty ()
				? null
				: textHelper.findOrCreate (valueParam);

		if (equal (oldValue, newValue)) {

			return textResponder.get ()
				.text (
					newValue != null
						? Html.newlineToBr (Html.encode (
							newValue.getText ()))
						: "");

		}

		// update note

		manualResponderNumber
			.setNotesText (newValue);

		// create event

		if (newValue != null) {

			eventLogic.createEvent (
				"object_field_updated",
				myUser,
				"notesText",
				manualResponderNumber,
				newValue);

		} else {

			eventLogic.createEvent (
				"object_field_nulled",
				myUser,
				"notesText",
				manualResponderNumber);

		}

		// finish off

		transaction.commit ();

		return textResponder.get ()
			.text (
				newValue != null
					? Html.newlineToBr (Html.encode (
						newValue.getText ()))
					: "");

	}

}
