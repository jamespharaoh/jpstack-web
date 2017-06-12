package wbs.apn.chat.contact.console;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.text.console.TextConsoleHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.text.web.TextResponder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.namednote.console.ChatNamedNoteConsoleHelper;
import wbs.apn.chat.namednote.console.ChatNamedNoteLogConsoleHelper;
import wbs.apn.chat.namednote.console.ChatNoteNameConsoleHelper;
import wbs.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;
import wbs.web.utils.HtmlUtils;

@PrototypeComponent ("chatMonitorInboxNamedNoteUpdateAction")
public
class ChatMonitorInboxNamedNoteUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatMonitorInboxConsoleHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatNamedNoteConsoleHelper chatNamedNoteHelper;

	@SingletonDependency
	ChatNamedNoteLogConsoleHelper chatNamedNoteLogHelper;

	@SingletonDependency
	ChatNoteNameConsoleHelper chatNoteNameHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceConsoleHelper serviceHelper;

	@SingletonDependency
	TextConsoleHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

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

			// lookup objects

			ChatMonitorInboxRec monitorInbox =
				chatMonitorInboxHelper.findFromContextRequired (
					transaction);

			ChatNoteNameRec chatNoteName =
				chatNoteNameHelper.findRequired (
					transaction,
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
					transaction,
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
					? textHelper.findOrCreate (
						transaction,
						newValue)
					: null;

			if (namedNote == null) {

				namedNote =
					chatNamedNoteHelper.insert (
						transaction,
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
						userConsoleLogic.userRequired (
							transaction))

					.setTimestamp (
						transaction.now ())

				);

			} else {

				namedNote

					.setText (
						newText)

					.setUser (
						userConsoleLogic.userRequired (
							transaction))

					.setTimestamp (
						transaction.now ());

			}

			chatNamedNoteLogHelper.insert (
				transaction,
				chatNamedNoteLogHelper.createInstance ()

				.setChatNamedNote (
					namedNote)

				.setText (
					newText)

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

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

}
