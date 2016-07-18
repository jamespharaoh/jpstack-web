package wbs.platform.scaffold.logic;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.laterThan;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.orNull;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.utils.ThreadManager;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (fluent = true)
@SingletonComponent ("sliceLogic")
public
class SliceLogicImplementation
	implements
		BackgroundProcess,
		SliceLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	ThreadManager threadManager;

	// properties

	@Getter @Setter
	Boolean runAutomatically = true;

	// state

	Map<Integer,Optional<Instant>> nextTimestampBySlice =
		new HashMap<> ();

	Map<Integer,Optional<Instant>> nextUpdateTimestampBySlice =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void setup () {

		threadManager.startThread (
			this::mainLoop,
			"SliceLogic");

	}

	public
	void mainLoop () {

		for (;;) {

			try {

				Thread.sleep (
					1000);

				if (runAutomatically) {
					runNow ();
				}

			} catch (InterruptedException exception) {

				return;

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"unknown",
					"sliceLogic",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	// implementation

	@Override
	public synchronized
	void runNow () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SliceLogicImplementation.runNow ()",
				this);

		// iterate slices

		for (
			Integer sliceId
				: nextUpdateTimestampBySlice.keySet ()
		) {

			SliceRec slice =
				sliceHelper.findRequired (
					sliceId);

			// perform update

			slice

				.setCurrentQueueInactivityTime (
					ifNull (
						slice.getCurrentQueueInactivityTime (),
						orNull (
							nextTimestampBySlice.get (
								sliceId))))

				.setCurrentQueueInactivityUpdateTime (
					optionalRequired (
						nextUpdateTimestampBySlice.get (
							sliceId)));

		}

		nextTimestampBySlice.clear ();
		nextUpdateTimestampBySlice.clear ();

		transaction.commit ();

	}

	@Override
	public synchronized
	void updateSliceInactivityTimestamp (
			@NonNull SliceRec slice,
			@NonNull Optional<Instant> timestamp) {

		Transaction transaction =
			database.currentTransaction ();

		Optional<Instant> nextUpdateTimestamp =
			nextUpdateTimestampBySlice.getOrDefault (
				slice.getId (),
				Optional.absent ());

		// skip if we have a more recent update

		if (

			isPresent (
				nextUpdateTimestamp)

			&& laterThan (
				transaction.now (),
				nextUpdateTimestamp.get ())

		) {
			return;
		}

		// remember this update

		nextUpdateTimestampBySlice.put (
			slice.getId (),
			Optional.of (
				transaction.now ()));

		nextTimestampBySlice.put (
			slice.getId (),
			timestamp);

	}

}
