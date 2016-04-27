package wbs.services.ticket.core.console;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateObjectHelper;

@PrototypeComponent ("ticketPendingHistoryPart")
public
class TicketPendingHistoryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject @Named
	ConsoleModule ticketPendingConsoleModule;

	@Inject
	TicketStateObjectHelper ticketStateHelper;

	@Inject
	TicketObjectHelper ticketHelper;

	@Inject
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
			ticketHelper.find (
				requestContext.stuffInt ("ticketId"));
	}

	@Override
	public
	void renderHtmlBodyContent () {

		goSummary ();

		goStateSummary ();

		goTicketNotes ();

	}

	void goSummary () {

		printFormat (
			"<h3>Summary</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputTableRows (
			formatWriter,
			ticketFields,
			ticket,
			ImmutableMap.of ());

		printFormat (
			"</table>\n");

	}

	void goTicketNotes () {

		printFormat (
			"<h3>Notes</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Index</td>\n",
			"<th>Text</th>\n",
			"</tr>\n");

		for (
			TicketNoteRec ticketNote
				: ticket.getTicketNotes ()
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				ticketNoteFields,
				ticketNote,
				ImmutableMap.of (),
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	void goStateSummary () {

		printFormat (
			"<h3>State</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputTableRows (
			formatWriter,
			ticketStateFields,
			ticket.getTicketState (),
			ImmutableMap.of ());

		printFormat (
			"</table>\n");

	}

}
