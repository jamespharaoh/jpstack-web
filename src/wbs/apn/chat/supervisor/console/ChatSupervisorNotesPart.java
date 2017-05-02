package wbs.apn.chat.supervisor.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsPeriod;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;

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

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			statsPeriod =
				genericCastUnchecked (
					parameters.get (
						"statsPeriod"));

			chat =
				chatHelper.findFromContextRequired (
					transaction);

			// get notes

			chatContactNotes =
				chatContactNoteHelper.findByTimestamp (
					transaction,
					chat,
					statsPeriod.toInterval ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

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
					transaction,
					chatContactNote.getUser (),
					chatContactNote.getUser ().getChat ());

				consoleObjectManager.writeTdForObjectMiniLink (
					transaction,
					chatContactNote.getMonitor (),
					chatContactNote.getMonitor ().getChat ());

				consoleObjectManager.writeTdForObjectMiniLink (
					transaction,
					chatContactNote.getConsoleUser ());

				htmlTableCellWrite (
					timeFormatter.timestampTimezoneString (
						chatMiscLogic.timezone (
							transaction,
							chat),
						chatContactNote.getTimestamp ()));

				htmlTableRowClose ();

			}

			htmlTableCellClose ();

		}

	}

}
