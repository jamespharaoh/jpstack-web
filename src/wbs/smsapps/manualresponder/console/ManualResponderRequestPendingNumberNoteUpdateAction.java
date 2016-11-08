package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
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
import wbs.utils.web.HtmlUtils;

@PrototypeComponent ("manualResponderRequestPendingNumberNoteUpdateAction")
public
class ManualResponderRequestPendingNumberNoteUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ManualResponderConsoleHelper manualResponderHelper;

	@SingletonDependency
	ManualResponderNumberConsoleHelper manualResponderNumberHelper;

	@SingletonDependency
	NumberConsoleHelper numberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// constants

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
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// get params

		Matcher idMatcher =
			idPattern.matcher (
				requestContext.parameterRequired (
					"id"));

		if (! idMatcher.matches ()) {

			throw new RuntimeException (
				"Invalid id in post");

		}

		Long numberId =
			Long.parseLong (
				idMatcher.group (
					1));

		valueParam =
			stringTrim (
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
				requestContext.stuffInteger (
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
			optionalEqualOrNotPresentWithClass (
				TextRec.class,
				optionalFromNullable (
					oldValue),
				optionalFromNullable (
					newValue))
		) {

			return textResponderProvider.get ()

				.text (
					newValue != null
						? HtmlUtils.encodeNewlineToBr (
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

		return textResponderProvider.get ()

			.text (
				newValue != null
					? HtmlUtils.encodeNewlineToBr (
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
			optionalEqualOrNotPresentWithClass (
				TextRec.class,
				optionalFromNullable (
					oldValue),
				optionalFromNullable (
					newValue))
		) {

			return textResponderProvider.get ()

				.text (
					newValue != null
						? HtmlUtils.encodeNewlineToBr (
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

		return textResponderProvider.get ()

			.text (
				newValue != null
					? HtmlUtils.encodeNewlineToBr (
						newValue.getText ())
					: "");

	}

}
