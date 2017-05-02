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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

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
				ticketHelper.findFromContextRequired (
					transaction);

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

			formFieldLogic.outputTableRows (
				transaction,
				formatWriter,
				ticketFields,
				ticket,
				emptyMap ());

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

				formFieldLogic.outputTableCellsList (
					transaction,
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

			formFieldLogic.outputTableRows (
				transaction,
				formatWriter,
				ticketStateFields,
				ticket.getTicketState (),
				emptyMap ());

			htmlTableClose ();

		}

	}

}
