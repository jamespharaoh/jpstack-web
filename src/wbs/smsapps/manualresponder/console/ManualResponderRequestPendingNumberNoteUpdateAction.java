package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.trim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

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
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

@PrototypeComponent ("manualResponderRequestPendingNumberNoteUpdateAction")
public
class ManualResponderRequestPendingNumberNoteUpdateAction
	extends ConsoleAction {

	// dependencies

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
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponder;

	static
	Pattern idPattern =
		Pattern.compile (
			"manualResponderNumberNote([0-9]+)");

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	// state

	String valueParam;

	ManualResponderRec manualResponder;
	NumberRec number;
	ManualResponderNumberRec manualResponderNumber;
	SmsCustomerRec customer;

	// implementation

	@Override
	protected
	Responder goReal () {

		// get params

		Matcher idMatcher =
			idPattern.matcher (
				requestContext.parameterRequired (
					"id"));

		if (! idMatcher.matches ()) {

			throw new RuntimeException (
				"Invalid id in post");

		}

		int numberId =
			Integer.parseInt (
				idMatcher.group (1));

		valueParam =
			trim (
				requestContext.parameterRequired (
					"value"));

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ManualResponderRequestPendingNumberNoteUpdateAction.goReal ()",
				this);

		manualResponder =
			manualResponderHelper.findRequired (
				requestContext.stuffInt (
					"manualResponderId"));

		number =
			numberHelper.findRequired (
				numberId);

		// find or create number

		manualResponderNumber =
			manualResponderNumberHelper.findOrCreate (
				manualResponder,
				number);

		customer =
			manualResponderNumber.getSmsCustomer ();

		if (

			isNull (
				customer)

			|| isNotNull (
				manualResponderNumber.getNotesText ())

		) {

			return updateNumber (
				transaction);

		} else {

			return updateCustomer (
				transaction);

		}

	}

	Responder updateNumber (
			@NonNull Transaction transaction) {

		// find old and new value

		TextRec oldValue =
			manualResponderNumber.getNotesText ();

		TextRec newValue =
			valueParam.isEmpty ()
				? null
				: textHelper.findOrCreate (
					valueParam);

		if (
			equal (
				oldValue,
				newValue)
		) {

			return textResponder.get ()

				.text (
					newValue != null
						? Html.encodeNewlineToBr (
							newValue.getText ())
						: "");

		}

		// update note

		manualResponderNumber

			.setNotesText (
				newValue);

		// create event

		if (newValue != null) {

			eventLogic.createEvent (
				"object_field_updated",
				userConsoleLogic.userRequired (),
				"notesText",
				manualResponderNumber,
				newValue);

		} else {

			eventLogic.createEvent (
				"object_field_nulled",
				userConsoleLogic.userRequired (),
				"notesText",
				manualResponderNumber);

		}

		// finish off

		transaction.commit ();

		return textResponder.get ()

			.text (
				newValue != null
					? Html.encodeNewlineToBr (
						newValue.getText ())
					: "");

	}

	Responder updateCustomer (
			@NonNull Transaction transaction) {

		// find old and new value

		TextRec oldValue =
			customer.getNotesText ();

		TextRec newValue =
			valueParam.isEmpty ()
				? null
				: textHelper.findOrCreate (
					valueParam);

		if (
			equal (
				oldValue,
				newValue)
		) {

			return textResponder.get ()

				.text (
					newValue != null
						? Html.encodeNewlineToBr (
							newValue.getText ())
						: "");

		}

		// update note

		customer

			.setNotesText (
				newValue);

		// create event

		if (newValue != null) {

			eventLogic.createEvent (
				"object_field_updated",
				userConsoleLogic.userRequired (),
				"notesText",
				customer,
				newValue);

		} else {

			eventLogic.createEvent (
				"object_field_nulled",
				userConsoleLogic.userRequired (),
				"notesText",
				customer);

		}

		// finish off

		transaction.commit ();

		return textResponder.get ()

			.text (
				newValue != null
					? Html.encodeNewlineToBr (
						newValue.getText ())
					: "");

	}

}
