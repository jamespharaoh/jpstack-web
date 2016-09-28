package wbs.services.ticket.core.console;

import static wbs.utils.web.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateObjectHelper;

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

	FormFieldSet ticketFields;
	FormFieldSet ticketNoteFields;
	FormFieldSet ticketStateFields;

	TicketRec ticket;

	// implementation

	@Override
	public
	void prepare () {

		// get field sets

		ticketFields =
			ticketPendingConsoleModule.formFieldSets ().get (
				"ticketFields");

		ticketNoteFields =
			ticketPendingConsoleModule.formFieldSets ().get (
				"ticketNoteFields");

		ticketStateFields =
			ticketPendingConsoleModule.formFieldSets ().get (
				"ticketStateFields");

		// load data

		ticket =
			ticketHelper.findRequired (
				requestContext.stuffInteger (
					"ticketId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
