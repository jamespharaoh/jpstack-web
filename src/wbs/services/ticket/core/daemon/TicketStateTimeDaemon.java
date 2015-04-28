package wbs.services.ticket.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.RandomLogic;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.sms.command.model.CommandObjectHelper;

@Log4j
@SingletonComponent ("ticketStateTimeDaemon")
public 
class TicketStateTimeDaemon 
	extends SleepingDaemonService {
	
	// dependencies
	
	@Inject
	TicketObjectHelper ticketHelper;
	
	@Inject
	ChatUserLogic chatUserLogic;
	
	@Inject
	CommandObjectHelper commandHelper;
	
	@Inject
	Database database;
	
	@Inject
	ExceptionLogic exceptionLogic;
	
	@Inject
	ObjectManager objectManager;
	
	@Inject
	RandomLogic randomLogic;
	
	@Inject
	ServiceObjectHelper serviceHelper;
	
	@Inject
	TextObjectHelper textHelper;
	
	@Inject
	Provider<QueueLogic> queueLogic;
	
	// details
	
	@Override
	protected
	String getThreadName () {
		return "ticketStateTime";
	}
	
	@Override
	protected
	int getDelayMs () {
		return 5 * 1000;
	}
	
	@Override
	protected
	String generalErrorSource () {
		return "time ticket check daemon";
	}
	
	@Override
	protected
	String generalErrorSummary () {
		return "error sending the ticket to it's state queue";
	}
	
	// implementation
	
	@Override
	protected
	void runOnce () {
	
		log.debug ("Getting all unqueued tickets");
	
		// get all the unqueued tickets
	
		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);
	
		List<TicketRec> tickets =
			ticketHelper.findUnqueuedTickets();
	
		transaction.close ();
	
		// then call doTicketTimeCheck for each one
	
		for (TicketRec ticket
				: tickets) {
	
			try {
	
				doTicketTimeCheck (
					ticket.getId ());
	
			} catch (Exception exception) {
	
				exceptionLogic.logThrowable (
					"daemon",
					"TicketStateTimeDaemon",
					exception,
					Optional.<Integer>absent (),
					false);
	
			}
	
		}
	
	}
	
	private
	void doTicketTimeCheck (
			int ticketId) {
	
		log.debug (
			stringFormat (
				"Checking timestamp for ticket",
				String.valueOf(ticketId)));
	
		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);
	
		// find the ticket
	
		TicketRec ticket =
			ticketHelper.find (ticketId);
			
		// check if the ticket is already in a queue
		
		if (ticket.getQueueItem() != null)
			return;
		
		// check if the ticket is ready to be queued
	
		Integer timeComparison =
			Instant.now ()
				.compareTo (ticket.getTimestamp ());
					
		if (timeComparison >= 0 && 
			ticket.getTicketState().getShowInQueue()) {		
				
			// create queue item

			QueueItemRec queueItem =
				queueLogic.get().createQueueItem (
					queueLogic.get().findQueue (
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
			
			ticket.setQueued (
				true);
			
		}		
	
		transaction.commit ();
	
	}

}
