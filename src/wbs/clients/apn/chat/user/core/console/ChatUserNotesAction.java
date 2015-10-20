package wbs.clients.apn.chat.user.core.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserNoteRec;
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
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserNotesAction")
public
class ChatUserNotesAction
	extends ConsoleAction {

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
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserNotesResponder");
	}

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec user =
			userHelper.find (
				requestContext.userId ());

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		// check params

		String noteString =
			requestContext.parameter ("note");

		if (noteString.trim ().length () == 0) {
			requestContext.addError ("Please enter a note");
			return null;
		}

		// create note

		TextRec noteText =
			textHelper.findOrCreate (noteString);

		chatUserNoteHelper.insert (
			new ChatUserNoteRec ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setUser (
				user)

			.setText (
				noteText)

		);

		// wrap up

		transaction.commit ();

		requestContext.addNotice ("Note added");

		return null;

	}

}
