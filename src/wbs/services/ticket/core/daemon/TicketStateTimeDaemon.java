package wbs.services.ticket.core.daemon;

import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.notLessThanZero;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.utils.random.RandomLogic;

@Log4j
@SingletonComponent ("ticketStateTimeDaemon")
public
class TicketStateTimeDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	TicketObjectHelper ticketHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	QueueLogic queueLogic;

	// details

	@Override
	protected
	String getThreadName () {
		return "ticketStateTime";
	}

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			5);

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

		// TODO disabled for now

		if (
			Boolean.parseBoolean (
				"true")
		) {
			return;
		}

		log.debug (
			"Getting all unqueued tickets");

		// get all the unqueued tickets

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"TicketStateTimeDaemon.runOnce ()",
				this);

		List<TicketRec> tickets =
			ticketHelper.findUnqueuedTickets ();

		transaction.close ();

		// then call doTicketTimeCheck for each one

		for (
			TicketRec ticket
				: tickets
		) {

			try {

				doTicketTimeCheck (
					ticket.getId ());

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"daemon",
					"TicketStateTimeDaemon",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	private
	void doTicketTimeCheck (
			@NonNull Long ticketId) {

		log.debug (
			stringFormat (
				"Checking timestamp for ticket",
				String.valueOf (
					ticketId)));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"TicketStateTimeDaemon.doTicketTImeCheck (ticketId)",
				this);

		// find the ticket

		TicketRec ticket =
			ticketHelper.findRequired (
				ticketId);

		// check if the ticket is already in a queue

		if (
			isNotNull (
				ticket.getQueueItem ())
		) {
			return;
		}

		// check if the ticket is ready to be queued

		Integer timeComparison =
			Instant.now ().compareTo (
				ticket.getTimestamp ());

		if (

			notLessThanZero (
				timeComparison)

			&& booleanEqual (
				ticket.getTicketState ().getShowInQueue (),
				true)

		) {

			// create queue item

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

			ticket.setQueued (
				true);

		}

		transaction.commit ();

	}

}
