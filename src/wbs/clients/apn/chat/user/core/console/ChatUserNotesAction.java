package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.isEmptyString;
import static wbs.framework.utils.etc.Misc.trim;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.clients.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserNotesAction")
public
class ChatUserNotesAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserNoteObjectHelper chatUserNoteHelper;

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserNotesResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserNotesAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		// check params

		String noteString =
			trim (
				requestContext.parameterRequired (
					"note"));

		if (
			isEmptyString (
				noteString)
		) {

			requestContext.addError (
				"Please enter a note");

			return null;

		}

		// create note

		TextRec noteText =
			textHelper.findOrCreate (noteString);

		chatUserNoteHelper.insert (
			chatUserNoteHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setUser (
				userConsoleLogic.userRequired ())

			.setText (
				noteText)

		);

		// wrap up

		transaction.commit ();

		requestContext.addNotice (
			"Note added");

		return null;

	}

}
