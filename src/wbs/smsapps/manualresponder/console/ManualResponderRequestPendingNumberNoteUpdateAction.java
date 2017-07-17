package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.web.utils.HtmlUtils.htmlEncodeNewlineToBr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

import wbs.web.responder.TextResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("manualResponderRequestPendingNumberNoteUpdateAction")
public
class ManualResponderRequestPendingNumberNoteUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderConsoleHelper manualResponderHelper;

	@SingletonDependency
	ManualResponderNumberConsoleHelper manualResponderNumberHelper;

	@SingletonDependency
	NumberConsoleHelper numberHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TextResponder> textResponderProvider;

	// constants

	static
	Pattern idPattern =
		Pattern.compile (
			"manualResponderNumberNote([0-9]+)");

	// state

	String valueParam;

	ManualResponderRec manualResponder;
	NumberRec number;
	ManualResponderNumberRec manualResponderNumber;
	SmsCustomerRec customer;

	// implementation

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

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

			manualResponder =
				manualResponderHelper.findFromContextRequired (
					transaction);

			number =
				numberHelper.findRequired (
					transaction,
					numberId);

			// find or create number

			manualResponderNumber =
				manualResponderNumberHelper.findOrCreate (
					transaction,
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

	}

	WebResponder updateNumber (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateNumber");

		) {

			// find old and new value

			TextRec oldValue =
				manualResponderNumber.getNotesText ();

			TextRec newValue =
				valueParam.isEmpty ()
					? null
					: textHelper.findOrCreate (
						transaction,
						valueParam);

			if (
				optionalEqualOrNotPresentWithClass (
					TextRec.class,
					optionalFromNullable (
						oldValue),
					optionalFromNullable (
						newValue))
			) {

				return textResponderProvider.provide (
					transaction,
					textResponder ->
						textResponder

					.text (
						newValue != null
							? htmlEncodeNewlineToBr (
								newValue.getText ())
							: "")

				);

			}

			// update note

			manualResponderNumber

				.setNotesText (
					newValue);

			// create event

			if (newValue != null) {

				eventLogic.createEvent (
					transaction,
					"object_field_updated",
					userConsoleLogic.userRequired (
						transaction),
					"notesText",
					manualResponderNumber,
					newValue);

			} else {

				eventLogic.createEvent (
					transaction,
					"object_field_nulled",
					userConsoleLogic.userRequired (
						transaction),
					"notesText",
					manualResponderNumber);

			}

			// finish off

			transaction.commit ();

			return textResponderProvider.provide (
				transaction,
				textResponder ->
					textResponder

				.text (
					newValue != null
						? htmlEncodeNewlineToBr (
							newValue.getText ())
						: "")

			);

		}

	}

	WebResponder updateCustomer (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateCussstomer");


		) {

			// find old and new value

			TextRec oldValue =
				customer.getNotesText ();

			TextRec newValue =
				valueParam.isEmpty ()
					? null
					: textHelper.findOrCreate (
						transaction,
						valueParam);

			if (
				optionalEqualOrNotPresentWithClass (
					TextRec.class,
					optionalFromNullable (
						oldValue),
					optionalFromNullable (
						newValue))
			) {

				return textResponderProvider.provide (
					transaction,
					textResponder ->
						textResponder

					.text (
						newValue != null
							? htmlEncodeNewlineToBr (
								newValue.getText ())
							: "")

				);

			}

			// update note

			customer

				.setNotesText (
					newValue);

			// create event

			if (newValue != null) {

				eventLogic.createEvent (
					transaction,
					"object_field_updated",
					userConsoleLogic.userRequired (
						transaction),
					"notesText",
					customer,
					newValue);

			} else {

				eventLogic.createEvent (
					transaction,
					"object_field_nulled",
					userConsoleLogic.userRequired (
						transaction),
					"notesText",
					customer);

			}

			// finish off

			transaction.commit ();

			return textResponderProvider.provide (
				transaction,
				textResponder ->
					textResponder

				.text (
					newValue != null
						? htmlEncodeNewlineToBr (
							newValue.getText ())
						: "")

			);

		}

	}

}
