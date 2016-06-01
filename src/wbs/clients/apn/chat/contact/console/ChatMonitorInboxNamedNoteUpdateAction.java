package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.namednote.model.ChatNamedNoteLogObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNamedNoteObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.Responder;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatMonitorInboxNamedNoteUpdateAction")
public
class ChatMonitorInboxNamedNoteUpdateAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	ChatNamedNoteObjectHelper chatNamedNoteHelper;

	@Inject
	ChatNamedNoteLogObjectHelper chatNamedNoteLogHelper;

	@Inject
	ChatNoteNameObjectHelper chatNoteNameHelper;

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

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponder;

	// misc

	static
	Pattern idPattern =
		Pattern.compile (
			"namedNote([0-9]+)(user|monitor)");

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
				requestContext.parameterOrNull (
					"id"));

		if (! idMatcher.matches ()) {

			throw new RuntimeException (
				"Invalid id in post");

		}

		int noteNameId =
			Integer.parseInt (
				idMatcher.group (1));

		String typeString =
			idMatcher.group (2);

		String newValue =
			requestContext.parameterOrNull ("value").trim ();

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatMonitorInboxRec monitorInbox =
			chatMonitorInboxHelper.find (
				requestContext.stuffInt (
					"chatMonitorInboxId"));

		ChatNoteNameRec chatNoteName =
			chatNoteNameHelper.find (
				noteNameId);

		// work out which user is which

		ChatUserRec thisChatUser;
		ChatUserRec otherChatUser;

		if (
			equal (
				typeString,
				"user")
		) {

			thisChatUser =
				monitorInbox.getUserChatUser ();

			otherChatUser =
				monitorInbox.getMonitorChatUser ();

		} else if (
			equal (
				typeString,
				"monitor")
		) {

			thisChatUser =
				monitorInbox.getMonitorChatUser ();

			otherChatUser =
				monitorInbox.getUserChatUser ();

		} else {

			throw new RuntimeException ();

		}

		// find old value

		ChatNamedNoteRec namedNote =
			chatNamedNoteHelper.find (
				thisChatUser,
				otherChatUser,
				chatNoteName);

		String oldValue =
			namedNote != null && namedNote.getText () != null
				? namedNote.getText ().getText ()
				: "";

		if (
			equal (
				newValue,
				oldValue)
		) {

			return textResponder.get ()

				.text (
					Html.encode (
						newValue));

		}

		// update note

		TextRec newText =
			newValue.length () > 0
				? textHelper.findOrCreate (newValue)
				: null;

		if (namedNote == null) {

			namedNote =
				chatNamedNoteHelper.insert (
					chatNamedNoteHelper.createInstance ()

				.setChatNoteName (
					chatNoteName)

				.setThisUser (
					thisChatUser)

				.setOtherUser (
					otherChatUser)

				.setText (
					newText)

				.setUser (
					userConsoleLogic.userRequired ())

				.setTimestamp (
					transaction.now ())

			);

		} else {

			namedNote

				.setText (
					newText)

				.setUser (
					userConsoleLogic.userRequired ())

				.setTimestamp (
					transaction.now ());

		}

		chatNamedNoteLogHelper.insert (
			chatNamedNoteLogHelper.createInstance ()

			.setChatNamedNote (
				namedNote)

			.setText (
				newText)

			.setUser (
				userConsoleLogic.userRequired ())

			.setTimestamp (
				transaction.now ())

		);

		transaction.commit ();

		return textResponder.get ()

			.text (
				Html.encode (
					newValue));

	}

}
