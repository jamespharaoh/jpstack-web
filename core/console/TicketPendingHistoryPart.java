package wbs.services.ticket.core.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import javax.inject.Named;

import lombok.NonNull;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateRec;

@PrototypeComponent ("ticketPendingHistoryPart")
public
class TicketPendingHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	@Named
	ConsoleModule ticketPendingConsoleModule;

	@SingletonDependency
	TicketStateConsoleHelper ticketStateHelper;

	@SingletonDependency
	TicketConsoleHelper ticketHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	FormFieldSet <TicketRec> ticketFields;
	FormFieldSet <TicketNoteRec> ticketNoteFields;
	FormFieldSet <TicketStateRec> ticketStateFields;

	TicketRec ticket;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		// get field sets

		ticketFields =
			ticketPendingConsoleModule.formFieldSetRequired (
				"ticketFields",
				TicketRec.class);

		ticketNoteFields =
			ticketPendingConsoleModule.formFieldSetRequired (
				"ticketNoteFields",
				TicketNoteRec.class);

		ticketStateFields =
			ticketPendingConsoleModule.formFieldSetRequired (
				"ticketStateFields",
				TicketStateRec.class);

		// load data

		ticket =
			ticketHelper.findFromContextRequired ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			goSummary (
				taskLogger);

			goStateSummary (
				taskLogger);

			goTicketNotes (
				taskLogger);

		}

	}

	void goSummary (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goSummary");

		) {

			htmlHeadingThreeWrite (
				"Summary");

			htmlTableOpenDetails ();

			formFieldLogic.outputTableRows (
				taskLogger,
				formatWriter,
				ticketFields,
				ticket,
				emptyMap ());

			htmlTableClose ();

		}

	}

	void goTicketNotes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goTicketNotes");

		) {

			htmlHeadingThreeWrite (
				"Notes");

			htmlTableOpenDetails ();

			htmlTableHeaderRowWrite (
				"Index",
				"Text");

			for (
				TicketNoteRec ticketNote
					: ticket.getTicketNotes ()
			) {

				htmlTableRowOpen ();

				formFieldLogic.outputTableCellsList (
					taskLogger,
					formatWriter,
					ticketNoteFields,
					ticketNote,
					emptyMap (),
					true);

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

	void goStateSummary (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goStateSummary");

		) {

			htmlHeadingThreeWrite (
				"State");

			htmlTableOpenDetails ();

			formFieldLogic.outputTableRows (
				taskLogger,
				formatWriter,
				ticketStateFields,
				ticket.getTicketState (),
				emptyMap ());

			htmlTableClose ();

		}

	}

}
