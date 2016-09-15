package wbs.apn.chat.supervisor.console;

import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.experimental.Accessors;

import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsPeriod;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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
	void prepare () {

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
	void renderHtmlBodyContent () {

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
