package wbs.services.ticket.core.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import org.joda.time.Instant;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketTemplateRec;

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
	UserConsoleLogic userConsoleLogic;

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
				"TicketPendingFormAction.goReal ()",
				this);

		// find message

		TicketRec ticket =
			ticketHelper.findRequired (
				requestContext.stuffInteger (
					"ticketId"));

		// sanity check

		if (ticket.getQueueItem () == null)
			throw new RuntimeException ();

		// select template

		TicketTemplateRec template;

		// action to be performed

		template =
			ticketTemplateHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"template"));

		// remove old queue item

		queueLogic.processQueueItem (
			ticket.getQueueItem (),
			userConsoleLogic.userRequired ());

		ticket

			.setQueueItem (
				null);

		// update ticket timestamp

		String timpestampString =
			requestContext.parameterRequired (
				stringFormat (
					"timestamp-%s",
					template.getTicketState ().getId ()));

		Integer timestamp =
			Integer.parseInt (
				timpestampString);

		if (
			timestamp >= template.getTicketState ().getMinimum ()
			&& timestamp <= template.getTicketState ().getMaximum ()
		) {

			// update ticket state

			ticket

				.setTicketState (
					template.getTicketState ());

			// set new timestamp

			ticket.setTimestamp(
				Instant.now ()
					.plus(timestamp * 1000));

			ticket.setQueued (
				false);

		} else {

			throw new RuntimeException (
				"Timestamp out of bounds");

		}

		// check if a new note was added

		String noteText =
			requestContext.parameterRequired (
				"note-text");

		if (! noteText.isEmpty ()) {

			ticketNoteHelper.insert (
				ticketNoteHelper.createInstance ()

				.setTicket (
					ticket)

				.setIndex (
					ticket.getNumNotes ())

				.setNoteText (
					noteText)

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
