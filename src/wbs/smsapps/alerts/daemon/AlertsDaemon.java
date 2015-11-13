package wbs.smsapps.alerts.daemon;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.StringSubstituter;
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
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.alerts.model.AlertsAlertObjectHelper;
import wbs.smsapps.alerts.model.AlertsNumberRec;
import wbs.smsapps.alerts.model.AlertsSettingsObjectHelper;
import wbs.smsapps.alerts.model.AlertsSettingsRec;
import wbs.smsapps.alerts.model.AlertsStatusCheckObjectHelper;
import wbs.smsapps.alerts.model.AlertsSubjectRec;

@Log4j
@SingletonComponent ("alertsDaemon")
public
class AlertsDaemon
	extends SleepingDaemonService {

	@Inject
	AlertsAlertObjectHelper alertsAlertHelper;

	@Inject
	AlertsSettingsObjectHelper alertsSettingsHelper;

	@Inject
	AlertsStatusCheckObjectHelper alertsStatusCheckHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueLogic queueLogic;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	Provider<MessageSender> messageSender;

	// details

	@Override
	protected
	String getThreadName () {
		return "Alerts";
	}

	@Override
	protected
	int getDelayMs () {
		return 20 * 1000;
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
				this);

		List<AlertsSettingsRec> alertsSettingss =
			alertsSettingsHelper.findAll ();

		List<Integer> alertsSettingsIds =
			new ArrayList<Integer> ();

		// work out current minute

		Instant currentMinute =
			transaction.now ()
				.toDateTime ()
				.withSecondOfMinute (0)
				.withMillisOfSecond (0)
				.toInstant ();

		// find alerts settings pending

		for (AlertsSettingsRec alertsSettings
				: alertsSettingss) {

			if (! alertsSettings.getEnabled ())
				continue;

			if (alertsSettings.getLastStatusCheck () != null
					&& alertsSettings.getLastStatusCheck ().isAfter (
						currentMinute))
				continue;

			alertsSettingsIds.add (
				alertsSettings.getId ());

		}

		transaction.close ();

		// process alerts settings

		for (Integer alertsSettingsId
				: alertsSettingsIds) {

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
					Optional.<Integer>absent (),
					false);

			}

		}

	}

	protected
	void runOnce (
			int alertsSettingsId) {

		log.debug (
			stringFormat (
				"checking if we should send an alert for %s",
				alertsSettingsId));

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		AlertsSettingsRec alertsSettings =
			alertsSettingsHelper.find (
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

		int numUnclaimed = 0;
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
					! in (
						queueItem.getState (),
						QueueItemState.pending,
						QueueItemState.waiting)
				) {

					continue;

				}

				Instant createdTime =
					dateToInstant (
						queueItem.getCreatedTime ());

				if (createdTime.isBefore (oldest))
					oldest = createdTime;

				numUnclaimed ++;

			}

		}

		Duration maxDurationFoundInQueue =
			new Duration (oldest, now);

		Integer maxDurationSeconds =
			maxDurationFoundInQueue
				.toStandardSeconds ()
				.getSeconds ();

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

			int numSent =
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
			Integer numUnclaimed,
			Duration maximumDuration) {

		Integer maxDurationMinutes =
			maximumDuration
				.toStandardMinutes ()
				.getMinutes ();

		String message =
			new StringSubstituter ()

				.param (
					"numItems",
					Integer.toString (numUnclaimed))

				.param (
					"numMinutes",
					Integer.toString (maxDurationMinutes))

				.substitute (
					alertsSettings.getTemplate ());

		log.warn (message);

		TextRec messageText =
			textHelper.findOrCreate (message);

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

		int startHour =
			ifNull (
				alertsSettings.getStartHour (),
				0);

		int endHour =
			ifNull (
				alertsSettings.getEndHour (),
				24);

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
				serviceHelper.findByCode (
					alertsSettings,
					"alerts");

			RouteRec route =
				routerLogic.resolveRouter (
					alertsSettings.getRouter ());

			messageSender.get ()

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
