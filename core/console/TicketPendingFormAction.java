package wbs.services.ticket.core.console;

import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.services.ticket.core.model.TicketNoteRec;
import wbs.services.ticket.core.model.TicketStateState;
import wbs.services.ticket.core.model.TicketTemplateRec;
import wbs.services.ticket.core.model.TicketRec;

@PrototypeComponent ("ticketPendingFormAction")
public
class TicketPendingFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	Database database;

	@Inject
	TicketConsoleHelper ticketHelper;
	
	@Inject
	TicketNoteConsoleHelper ticketNoteHelper;
	
	@Inject
	TicketTemplateConsoleHelper ticketTemplateHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"ticketPendingFormResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// find user

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// find message

		TicketRec ticket =
			ticketHelper.find (
				requestContext.stuffInt (
					"ticketId"));

		// sanity check

		if (ticket.getQueueItem () == null)
			throw new RuntimeException ();

		// select template

		String templateString =
			requestContext.parameter ("template");

		TicketTemplateRec template;

		// action to be performed

		template =
			ticketTemplateHelper.find (
				toInteger (templateString));

		if (template == null)
			throw new RuntimeException ();		
		
		// remove old queue item

		queueLogic.processQueueItem (
			ticket.getQueueItem (),
			myUser);
		
		// if the ticket was not already closed
		
		if (ticket.getTicketState().getState() != TicketStateState.closed) {
			
			// update ticket state
			
			ticket.setTicketState(
				template.getTicketState());
			
			// create item for the new state queue if the state allows it
			
			if (ticket.getTicketState().getShowInQueue()) {

				QueueItemRec queueItem =
					queueLogic.createQueueItem (
						queueLogic.findQueue (
							ticket.getTicketState (),
							"default"),
						ticket,
						ticket,
						ticket.getCode (),
						ticket.getTicketState().toString());
	
				// add queue item to ticket
	
				ticket
					.setQueueItem (
						queueItem);
				
			}
			
		}
		
		// check if a new note was added

		String noteText =
				requestContext.parameter ("note-text");
		
		if (!noteText.isEmpty()) {
			
			ticketNoteHelper.insert (
				new TicketNoteRec ()

					.setTicket (
						ticket)
			
					.setIndex (
						ticket.getNumNotes ())
					
					.setNoteText(noteText)
			
				);
			
			ticket
			.setNumNotes (
				ticket.getNumNotes () + 1);
			
		}
		
		// done

		transaction.commit ();

		requestContext.addNotice (
			"Ticket state changed to " +
		    template.getTicketState().toString());

		// return

		return responder (
			"queueHomeResponder");

	}


}
