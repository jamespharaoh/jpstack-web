package wbs.services.ticket.core.console;

import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateObjectHelper;
import wbs.services.ticket.core.model.TicketStateRec;

@PrototypeComponent ("ticketPendingHistoryPart")
public
class TicketPendingHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	@Named
	ConsoleModule ticketPendingConsoleModule;

	@SingletonDependency
	TicketStateObjectHelper ticketStateHelper;

	@SingletonDependency
	TicketObjectHelper ticketHelper;

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
			ticketPendingConsoleModule.formFieldSet (
				"ticketFields",
				TicketRec.class);

		ticketNoteFields =
			ticketPendingConsoleModule.formFieldSet (
				"ticketNoteFields",
				TicketNoteRec.class);

		ticketStateFields =
			ticketPendingConsoleModule.formFieldSet (
				"ticketStateFields",
				TicketStateRec.class);

		// load data

		ticket =
			ticketHelper.findRequired (
				requestContext.stuffInteger (
					"ticketId"));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		goSummary ();

		goStateSummary ();

		goTicketNotes ();

	}

	void goSummary () {

		htmlHeadingThreeWrite (
			"Summary");

		htmlTableOpenDetails ();

		formFieldLogic.outputTableRows (
			formatWriter,
			ticketFields,
			ticket,
			ImmutableMap.of ());

		htmlTableClose ();

	}

	void goTicketNotes () {

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
				formatWriter,
				ticketNoteFields,
				ticketNote,
				ImmutableMap.of (),
				true);

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

	void goStateSummary () {

		htmlHeadingThreeWrite (
			"State");

		htmlTableOpenDetails ();

		formFieldLogic.outputTableRows (
			formatWriter,
			ticketStateFields,
			ticket.getTicketState (),
			ImmutableMap.of ());

		htmlTableClose ();

	}

}
