package wbs.apn.chat.supervisor.console;

import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsPeriod;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.utils.time.TimeFormatter;

@Accessors (fluent = true)
@PrototypeComponent ("chatSupervisorNotesPart")
public
class ChatSupervisorNotesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatContactNoteObjectHelper chatContactNoteHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	StatsPeriod statsPeriod;

	ChatRec chat;

	List <ChatContactNoteRec> chatContactNotes;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		statsPeriod =
			(StatsPeriod)
			parameters.get ("statsPeriod");

		chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		// get notes

		chatContactNotes =
			chatContactNoteHelper.findByTimestamp (
				chat,
				statsPeriod.toInterval ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Note",
			"User",
			"Monitor",
			"By",
			"Timestamp");

		for (
			ChatContactNoteRec chatContactNote
				: chatContactNotes
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				chatContactNote.getNotes ());

			consoleObjectManager.writeTdForObjectMiniLink (
				chatContactNote.getUser (),
				chatContactNote.getUser ().getChat ());

			consoleObjectManager.writeTdForObjectMiniLink (
				chatContactNote.getMonitor (),
				chatContactNote.getMonitor ().getChat ());

			consoleObjectManager.writeTdForObjectMiniLink (
				chatContactNote.getConsoleUser ());

			htmlTableCellWrite (
				timeFormatter.timestampTimezoneString (
					chatMiscLogic.timezone (
						chat),
					chatContactNote.getTimestamp ()));

			htmlTableRowClose ();

		}

		htmlTableCellClose ();

	}

}
