package wbs.clients.apn.chat.namednote.console;

import static wbs.framework.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsNotEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import lombok.Cleanup;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

@PrototypeComponent ("chatNoteNamesAction")
public
class ChatNoteNamesAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatNoteNameObjectHelper chatNoteNameHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatNoteNamesResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		List<String> notices =
			new ArrayList<String> ();

		// setup transaction etc

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatNoteNamesAction.goReal ()",
				this);

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		List <ChatNoteNameRec> chatNoteNames =
			chatNoteNameHelper.findNotDeleted (
				chat);

		if (requestContext.getForm ("saveChanges") != null) {

			// update note names

			int numUpdated = 0;

			for (
				ChatNoteNameRec noteName
					: chatNoteNames
			) {

				String formKey =
					stringFormat (
						"noteName%d",
						noteName.getId ());

				String newName =
					requestContext.getForm (
						formKey);

				if (newName == null)
					continue;

				if (
					stringEqualSafe (
						newName,
						noteName.getName ())
				) {
					continue;
				}

				noteName

					.setName (
						newName);

				numUpdated ++;

			}

			if (numUpdated == 1)
				notices.add ("Note name updated");

			if (numUpdated > 1) {

				notices.add (
					stringFormat (
						"%d note names updated",
						numUpdated));

			}

			// add new note

			if (
				stringIsNotEmpty (
					requestContext.getForm (
						"noteNameNew"))
			) {

				chatNoteNameHelper.insert (
					chatNoteNameHelper.createInstance ()

					.setChat (
						chat)

					.setIndex (
						fromJavaInteger (
							chatNoteNames.size ()))

					.setName (
						requestContext.getForm (
							"noteNameNew"))

					.setDescription (
						"")

					.setDeleted (
						false)

				);

				notices.add ("New name added");

				requestContext.setEmptyFormData ();

			}

		} else for (ChatNoteNameRec chatNoteName
				: chatNoteNames) {

			String noteMoveUpKey =
				stringFormat (
					"noteMoveUp%d",
					chatNoteName.getId ());

			if (requestContext.getForm (noteMoveUpKey) != null
					&& chatNoteName.getIndex () > 0) {

				long index =
					chatNoteName.getIndex ();

				ChatNoteNameRec otherNote =
					chatNoteNames.get (
						toJavaIntegerRequired (
							index - 1));

				// first set to non-conflicting values

				chatNoteName.setIndex (
					-1l);

				otherNote.setIndex (
					-2l);

				transaction.flush ();

				// then to new values

				chatNoteName.setIndex (
					index - 1);

				otherNote.setIndex (
					index);

				// done

				notices.add (
					"Note moved up");

				break;

			}

			String noteMoveDownKey =
				stringFormat (
					"noteMoveDown%d",
					chatNoteName.getId ());

			if (requestContext.getForm (noteMoveDownKey)
						!= null
					&& chatNoteName.getIndex ()
						< chatNoteNames.size () - 1) {

				long index =
					chatNoteName.getIndex ();

				ChatNoteNameRec otherNote =
					chatNoteNames.get (
						toJavaIntegerRequired (
							index + 1));

				chatNoteName.setIndex (
					-1l);

				otherNote.setIndex (
					-2l);

				transaction.flush ();

				chatNoteName.setIndex (
					index + 1);

				otherNote.setIndex (
					index);

				notices.add (
					"Note moved down");

				break;

			}

			String noteDeleteKey =
				stringFormat (
					"noteDelete%d",
					chatNoteName.getId ());

			if (requestContext.getForm (noteDeleteKey) != null) {

				long index =
					chatNoteName.getIndex ();

				chatNoteName.setIndex (
					null);

				chatNoteName.setDeleted (
					true);

				transaction.flush ();

				for (
					long otherIndex = index;
					otherIndex < chatNoteNames.size () - 1;
					otherIndex ++
				) {

					ChatNoteNameRec otherChatNoteName =
						chatNoteNames.get (
							toJavaIntegerRequired (
								otherIndex + 1));

					otherChatNoteName.setIndex (
						otherIndex + 1);

				}

				notices.add (
					"Note deleted");

				break;

			}

		}

		// commit and finish up

		transaction.commit ();

		requestContext.addNotices (notices);

		return null;

	}

}
