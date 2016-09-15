package wbs.smsapps.alerts.daemon;

import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.alerts.model.AlertsAlertObjectHelper;
import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsObjectHelper;
import wbs.smsapps.alerts.model.AlertsSettingsRec;
import wbs.smsapps.alerts.model.AlertsStatusCheckObjectHelper;
import wbs.smsapps.alerts.model.AlertsSubjectRec;
import wbs.utils.string.StringSubstituter;

@Log4j
@SingletonComponent ("alertsDaemon")
public
class AlertsDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	AlertsAlertObjectHelper alertsAlertHelper;

	@SingletonDependency
	AlertsSettingsObjectHelper alertsSettingsHelper;

	@SingletonDependency
	AlertsStatusCheckObjectHelper alertsStatusCheckHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	QueueSubjectObjectHelper queueSubjectHelper;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// details

	@Override
	protected
	String getThreadName () {
		return "Alerts";
	}

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			20);

	}

	@Override
	protected
	String generalErrorSource () {
		return "alerts daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error running alerts background process";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"AlertsDaemon.runOnce ()",
				this);

		// work out current minute

		Instant currentMinute =
			transaction.now ()
				.toDateTime ()
				.withSecondOfMinute (0)
				.withMillisOfSecond (0)
				.toInstant ();

		// find alerts settings pending

		List<Long> alertsSettingsIds =
			alertsSettingsHelper.findAll ().stream ()

			.filter (
				alertsSettings ->
					! alertsSettings.getDeleted ())

			.filter (
				alertsSettings ->
					alertsSettings.getEnabled ())

			.filter (
				alertsSettings ->

				isNull (
					alertsSettings.getLastStatusCheck ())

				|| earlierThan (
					alertsSettings.getLastStatusCheck (),
					currentMinute)

			)

			.map (
				alertsSettings ->
					alertsSettings.getId ())

			.collect (
				Collectors.toList ());

		transaction.close ();

		// process alerts settings

		for (
			Long alertsSettingsId
				: alertsSettingsIds
		) {

			try {

				runOnce (
					alertsSettingsId);

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error checking alerts for %s",
						alertsSettingsId),
					exception);

				exceptionLogger.logThrowable (
					"daemon",
					"alerts daemon " + alertsSettingsId,
					exception,
					Optional.absent (),
					GenericExceptionResolution.ignoreWithNoWarning);

			}

		}

	}

	protected
	void runOnce (
			@NonNull Long alertsSettingsId) {

		log.debug (
			stringFormat (
				"checking if we should send an alert for %s",
				alertsSettingsId));

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"AlertsDaemon.runOnce ()",
				this);

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.findRequired (
				alertsSettingsId);

		// check it's still pending

		if (! alertsSettings.getEnabled ())
			return;

		Instant currentMinute =
			transaction.now ()
				.toDateTime ()
				.withSecondOfMinute (0)
				.withMillisOfSecond (0)
				.toInstant ();

		if (alertsSettings.getLastStatusCheck () != null
				&& alertsSettings.getLastStatusCheck ().isAfter (
					currentMinute))
			return;

		// durations

		Duration maxDurationAllowedInQueue =
			new Duration (
				alertsSettings.getMaxDurationInQueue ()
				* 1000L);

		// look up subjects

		Map<Record<?>,AlertsSubjectRec> subjects =
			new HashMap<Record<?>,AlertsSubjectRec> ();

		for (AlertsSubjectRec alertsSubject
				: alertsSettings.getAlertsSubjects ()) {

			Record<?> subject =
				objectManager.findObject (
					new GlobalId (
						alertsSubject.getObjectType ().getId (),
						alertsSubject.getObjectId ()));

			subjects.put (
				subject,
				alertsSubject);

		}

		// get queue info

		long numUnclaimed = 0;

		Instant now = Instant.now ();
		Instant oldest = now;

		for (
			QueueSubjectRec queueSubject
				: queueSubjectHelper.findActive ()
		) {

			if (
				! isSubject (
					subjects,
					queueSubject.getQueue ())
			) {

				continue;

			}

			for (
				QueueItemRec queueItem
					: queueLogic.getActiveQueueItems (
						queueSubject)
			) {

				if (
					enumNotInSafe (
						queueItem.getState (),
						QueueItemState.pending,
						QueueItemState.waiting)
				) {

					continue;

				}

				Instant createdTime =
					queueItem.getCreatedTime ();

				if (createdTime.isBefore (oldest))
					oldest = createdTime;

				numUnclaimed ++;

			}

		}

		Duration maxDurationFoundInQueue =
			new Duration (
				oldest,
				now);

		Long maxDurationSeconds =
			fromJavaInteger (
				maxDurationFoundInQueue
					.toStandardSeconds ()
					.getSeconds ());

		log.debug (
			stringFormat (
				"now is %s",
				now));

		log.debug (
			stringFormat (
				"oldest is %s",
				oldest));

		log.debug (
			stringFormat (
				"oldest duration is %s",
				maxDurationFoundInQueue));

		log.debug (
			stringFormat (
				"unclaimed count is %s",
				numUnclaimed));

		// check if alert is due

		boolean maxDurationExceeded =
			maxDurationFoundInQueue.isLongerThan (
				maxDurationAllowedInQueue);

		boolean maxItemsExceeded =
			numUnclaimed > alertsSettings.getMaxItemsInQueue ();

		boolean alertDue;

		if (! maxDurationExceeded
				&& ! maxItemsExceeded) {

			log.debug (
				stringFormat (
					"everything within limits"));

			alertDue = false;

		} else if (maxDurationExceeded) {

			log.debug (
				stringFormat (
					"maximum duration of %s exceeded",
					maxDurationAllowedInQueue));

			alertDue = true;

		} else if (maxItemsExceeded) {

			log.debug (
				stringFormat (
					"maximum unclaimed count of %s exceeded",
					alertsSettings.getMaxItemsInQueue ()));

			alertDue = true;

		} else {

			throw new RuntimeException ();

		}

		// construct message

		TextRec messageText =
			constructMessage (
				alertsSettings,
				numUnclaimed,
				maxDurationFoundInQueue);

		// limit send frequency

		boolean sentRecently =
			checkSentRecently (transaction, alertsSettings);

		boolean active =
			checkActive (transaction, alertsSettings);

		boolean performSend =
			alertDue && ! sentRecently && active;

		if (performSend) {

			long numSent =
				sendAlerts (
					alertsSettings,
					messageText);

			// create alert

			alertsAlertHelper.insert (
				alertsAlertHelper.createInstance ()

				.setAlertsSettings (
					alertsSettings)

				.setIndex (
					alertsSettings.getNumAlerts ())

				.setTimestamp (
					now)

				.setUnclaimedItems (
					numUnclaimed)

				.setMaximumDuration (
					maxDurationSeconds)

				.setText (
					messageText)

				.setRecipients (
					numSent)

			);

			// update alerts settings

			alertsSettings

				.setLastAlert (
					transaction.now ())

				.setNumAlerts (
					alertsSettings.getNumAlerts () + 1);

		}

		// create status check log

		alertsStatusCheckHelper.insert (
			alertsStatusCheckHelper.createInstance ()

			.setAlertsSettings (
				alertsSettings)

			.setIndex (
				alertsSettings.getNumStatusChecks ())

			.setTimestamp (
				transaction.now ())

			.setUnclaimedItems (
				numUnclaimed)

			.setMaximumDuration (
				maxDurationSeconds)

			.setResult (
				alertDue)

			.setActive (
				active)

			.setSentRecently (
				sentRecently)

			.setAlertSent (
				performSend)

		);

		// update alerts settings

		alertsSettings

			.setLastStatusCheck (
				transaction.now ())

			.setNumStatusChecks (
				alertsSettings.getNumStatusChecks () + 1);

		// and we're done

		transaction.commit ();

	}

	TextRec constructMessage (
			AlertsSettingsRec alertsSettings,
			Long numUnclaimed,
			Duration maximumDuration) {

		Integer maxDurationMinutes =
			maximumDuration
				.toStandardMinutes ()
				.getMinutes ();

		String message =
			new StringSubstituter ()

			.param (
				"numItems",
				Long.toString (
					numUnclaimed))

			.param (
				"numMinutes",
				Integer.toString (
					maxDurationMinutes))

			.substitute (
				alertsSettings.getTemplate ());

		log.warn (message);

		TextRec messageText =
			textHelper.findOrCreate (
				message);

		return messageText;

	}

	boolean checkSentRecently (
			Transaction transaction,
			AlertsSettingsRec alertsSettings) {

		Duration minAlertFrequency =
			new Duration (
				alertsSettings.getMaxAlertFrequency ()
				* 1000L);

		if (alertsSettings.getLastAlert () == null)
			return false;

		log.debug (
			stringFormat (
				"last alert was at %s",
				alertsSettings.getLastAlert ()));

		Duration durationSinceLastSend =
			new Duration (
				alertsSettings.getLastAlert (),
				transaction.now ());

		log.debug (
			stringFormat (
				"duration since last alert is %s",
				durationSinceLastSend));

		if (durationSinceLastSend.isShorterThan (minAlertFrequency)) {

			log.debug (
				stringFormat (
					"mininum alert frequency of %s not exceeded",
					minAlertFrequency));

			return true;

		} else {

			log.debug (
				stringFormat (
					"minimum alert frequency of %s exceeded",
					minAlertFrequency));

			return false;

		}

	}


	boolean checkActive (
			Transaction transaction,
			AlertsSettingsRec alertsSettings) {

		// check time

		long startHour =
			ifNull (
				alertsSettings.getStartHour (),
				0l);

		long endHour =
			ifNull (
				alertsSettings.getEndHour (),
				24l);

		int currentHour =
			transaction.now ()
				.toDateTime ()
				.getHourOfDay ();

		if (! (
			(
				startHour < endHour
				&& startHour <= currentHour
				&& currentHour < endHour
			) || (
				endHour < startHour
				&& (
					currentHour < endHour
					|| startHour <= currentHour
				)
			)
		)) {

			return false;

		}

		return true;

	}

	public
	boolean isSubject (
			Map<Record<?>,AlertsSubjectRec> subjects,
			Record<?> object) {

		Record<?> parent =
			objectManager.firstParent (
				object,
				subjects.keySet ());

		if (parent == null)
			return false;

		AlertsSubjectRec alertsSubject =
			subjects.get (parent);

		return alertsSubject.getInclude ();

	}

	int sendAlerts (
			AlertsSettingsRec alertsSettings,
			TextRec messageText) {

		// send alerts

		int numSent = 0;

		for (AlertsNumberRec alertsNumber
				: alertsSettings.getAlertsNumbers ()) {

			if (! alertsNumber.getEnabled ())
				continue;

			log.info (
				stringFormat (
					"sending alert to %s",
					alertsNumber.getNumber ().getNumber ()));

			ServiceRec alertsService =
				serviceHelper.findByCodeRequired (
					alertsSettings,
					"alerts");

			RouteRec route =
				routerLogic.resolveRouter (
					alertsSettings.getRouter ());

			messageSenderProvider.get ()

				.number (
					alertsNumber.getNumber ())

				.messageText (
					messageText)

				.numFrom (
					alertsSettings.getNumFrom ())

				.route (
					route)

				.service (
					alertsService)

				.send ();

			numSent ++;

		}

		return numSent;

	}

}
