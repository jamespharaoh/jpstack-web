package wbs.services.ticket.core.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import lombok.NonNull;

import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateRec;

@PrototypeComponent ("ticketPendingHistoryPart")
public
class TicketPendingHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	@NamedDependency ("ticketPendingHistoryTicketFormContextBuilder")
	FormContextBuilder <TicketRec> ticketFormContextBuilder;

	@SingletonDependency
	@NamedDependency ("ticketPendingHistoryNoteFormContextBuilder")
	FormContextBuilder <TicketNoteRec> ticketNoteFormContextBuilder;

	@SingletonDependency
	@NamedDependency ("ticketPendingHistorySateFormContextBuilder")
	FormContextBuilder <TicketStateRec> ticketStateFormContextBuilder;

	@SingletonDependency
	TicketStateConsoleHelper ticketStateHelper;

	@SingletonDependency
	TicketConsoleHelper ticketHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	FormContext <TicketRec> ticketFormContext;
	FormContext <TicketNoteRec> ticketNoteFormContext;
	FormContext <TicketStateRec> ticketStateFormContext;

	TicketRec ticket;

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

			// load data

			ticket =
				ticketHelper.findFromContextRequired (
					transaction);

			// form contexts

			ticketFormContext =
				ticketFormContextBuilder.build (
					transaction,
					emptyMap (),
					ticket);

			ticketNoteFormContext =
				ticketNoteFormContextBuilder.build (
					transaction,
					emptyMap ());

			ticketStateFormContext =
				ticketStateFormContextBuilder.build (
					transaction,
					emptyMap ());

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

			goSummary (
				transaction);

			goStateSummary (
				transaction);

			goTicketNotes (
				transaction);

		}

	}

	void goSummary (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goSummary");

		) {

			htmlHeadingThreeWrite (
				"Summary");

			htmlTableOpenDetails ();

			ticketFormContext.outputTableRows (
				transaction);

			htmlTableClose ();

		}

	}

	private
	void goTicketNotes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
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

				ticketNoteFormContext.outputTableCellsList (
					transaction,
					ticketNote,
					true);

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

	void goStateSummary (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goStateSummary");

		) {

			htmlHeadingThreeWrite (
				"State");

			htmlTableOpenDetails ();

			ticketStateFormContext.outputTableRows (
				transaction,
				ticket.getTicketState ());

			htmlTableClose ();

		}

	}

}
