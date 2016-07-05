package wbs.clients.apn.chat.namednote.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameObjectHelper;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

@PrototypeComponent ("chatNoteNamesAction")
public
class ChatNoteNamesAction
	extends ConsoleAction {

	@Inject
	Database database;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatNoteNameObjectHelper chatNoteNameHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatNoteNamesResponder");
	}

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
				this);

		ChatRec chat =
			chatHelper.findOrNull (
				requestContext.stuffInt ("chatId"));

		List<ChatNoteNameRec> chatNoteNames =
			chatNoteNameHelper.findNotDeleted (
				chat);

		if (requestContext.getForm ("saveChanges") != null) {

			// update note names

			int numUpdated = 0;

			for (ChatNoteNameRec noteName
					: chatNoteNames) {

				String formKey =
					stringFormat (
						"noteName%d",
						noteName.getId ());

				String newName =
					requestContext.getForm (formKey);

				if (newName == null)
					continue;

				if (equal (
						newName,
						noteName.getName ()))
					continue;

				noteName
					.setName (newName);

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
				isNotEmpty (
					requestContext.getForm (
						"noteNameNew"))
			) {

				chatNoteNameHelper.insert (
					chatNoteNameHelper.createInstance ()

					.setChat (
						chat)

					.setIndex (
						chatNoteNames.size ())

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

				int index =
					chatNoteName.getIndex ();

				ChatNoteNameRec otherNote =
					chatNoteNames.get (index - 1);

				chatNoteName.setIndex (- 1);
				otherNote.setIndex (- 2);

				transaction.flush ();

				chatNoteName.setIndex (index - 1);
				otherNote.setIndex (index);

				notices.add ("Note moved up");

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

				int index =
					chatNoteName.getIndex ();

				ChatNoteNameRec otherNote =
					chatNoteNames.get (index + 1);

				chatNoteName.setIndex (- 1);
				otherNote.setIndex (- 2);

				transaction.flush ();

				chatNoteName.setIndex (index + 1);
				otherNote.setIndex (index);

				notices.add ("Note moved down");

				break;

			}

			String noteDeleteKey =
				stringFormat (
					"noteDelete%d",
					chatNoteName.getId ());

			if (requestContext.getForm (noteDeleteKey) != null) {

				int index =
					chatNoteName.getIndex ();

				chatNoteName.setIndex (null);
				chatNoteName.setDeleted (true);

				transaction.flush ();

				for (int i = index; i < chatNoteNames.size () - 1; i ++)
					chatNoteNames.get (i + 1).setIndex (i);

				notices.add ("Note deleted");

				break;

			}

		}

		// commit and finish up

		transaction.commit ();

		requestContext.addNotices (notices);

		return null;

	}

}
