package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.trim;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.text.console.TextConsoleHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("imChatPendingCustomerNoteUpdateAction")
public
class ImChatPendingCustomerNoteUpdateAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatMessageConsoleHelper imChatMessageHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	EventLogic eventLogic;

	@Inject
	TextConsoleHelper textHelper;

	@Inject
	Provider<TextResponder> textResponderProvider;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal () {

		// get params

		String valueParam =
			trim (
				requestContext.parameterRequired (
					"value"));

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ImChatMessageRec message =
			imChatMessageHelper.findOrNull (
				requestContext.stuffInt (
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
			equal (
				oldValue,
				newValue)
		) {

			return textResponderProvider.get ()

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

		return textResponderProvider.get ()

			.text (
				newValue != null
					? Html.encodeNewlineToBr (
						newValue.getText ())
					: "");

	}

}
