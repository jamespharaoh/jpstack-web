package wbs.apn.chat.contact.console;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.namednote.model.ChatNamedNoteLogObjectHelper;
import wbs.apn.chat.namednote.model.ChatNamedNoteObjectHelper;
import wbs.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.utils.web.HtmlUtils;

@PrototypeComponent ("chatMonitorInboxNamedNoteUpdateAction")
public
class ChatMonitorInboxNamedNoteUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatNamedNoteObjectHelper chatNamedNoteHelper;

	@SingletonDependency
	ChatNamedNoteLogObjectHelper chatNamedNoteLogHelper;

	@SingletonDependency
	ChatNoteNameObjectHelper chatNoteNameHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponder;

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

		Long noteNameId =
			Long.parseLong (
				idMatcher.group (
					1));

		String typeString =
			idMatcher.group (
				2);

		String newValue =
			stringTrim (
				requestContext.parameterRequired (
					"value"));

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatMonitorInboxNamedNoteUpdateAction.goReal ()",
				this);

		ChatMonitorInboxRec monitorInbox =
			chatMonitorInboxHelper.findRequired (
				requestContext.stuffInteger (
					"chatMonitorInboxId"));

		ChatNoteNameRec chatNoteName =
			chatNoteNameHelper.findRequired (
				noteNameId);

		// work out which user is which

		ChatUserRec thisChatUser;
		ChatUserRec otherChatUser;

		if (
			stringEqualSafe (
				typeString,
				"user")
		) {

			thisChatUser =
				monitorInbox.getUserChatUser ();

			otherChatUser =
				monitorInbox.getMonitorChatUser ();

		} else if (
			stringEqualSafe (
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
			stringEqualSafe (
				newValue,
				oldValue)
		) {

			return textResponder.get ()

				.text (
					HtmlUtils.htmlEncode (
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
				HtmlUtils.htmlEncode (
					newValue));

	}

}
