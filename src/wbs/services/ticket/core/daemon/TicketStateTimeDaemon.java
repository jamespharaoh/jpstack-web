package wbs.services.ticket.core.daemon;

import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notLessThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;

@SingletonComponent ("ticketStateTimeDaemon")
public
class TicketStateTimeDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	TicketObjectHelper ticketHelper;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "ticket.state-time";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		// TODO disabled for now

		if (
			Boolean.parseBoolean (
				"true")
		) {
			return;
		}

		taskLogger.debugFormat (
			"Getting all unqueued tickets");

		// get all the unqueued tickets

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"TicketStateTimeDaemon.runOnce ()",
					this);

		) {

			List <TicketRec> tickets =
				ticketHelper.findUnqueuedTickets ();

			transaction.close ();

			// then call doTicketTimeCheck for each one

			for (
				TicketRec ticket
					: tickets
			) {

				try {

					doTicketTimeCheck (
						taskLogger,
						ticket.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						taskLogger,
						"daemon",
						"TicketStateTimeDaemon",
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	private
	void doTicketTimeCheck (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long ticketId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doTicketTimeCheck");

		taskLogger.debugFormat (
			"Checking timestamp for ticket",
			integerToDecimalString (
				ticketId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"TicketStateTimeDaemon.doTicketTImeCheck (ticketId)",
					this);

		) {

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
						taskLogger,
						ticket.getTicketState (),
						"default",
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

}
