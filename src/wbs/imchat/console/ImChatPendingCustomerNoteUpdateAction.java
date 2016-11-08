package wbs.imchat.console;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.text.console.TextConsoleHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.web.HtmlUtils;

@PrototypeComponent ("imChatPendingCustomerNoteUpdateAction")
public
class ImChatPendingCustomerNoteUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	TextConsoleHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider<TextResponder> textResponderProvider;

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// get params

		String valueParam =
			stringTrim (
				requestContext.parameterRequired (
					"value"));

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatPendingCustomerNoteUpdateAction.goReal ()",
				this);

		ImChatMessageRec message =
			imChatMessageHelper.findRequired (
				requestContext.stuffInteger (
					"imChatMessageId"));

		ImChatConversationRec conversation =
			message.getImChatConversation ();

		ImChatCustomerRec customer =
			conversation.getImChatCustomer ();

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
