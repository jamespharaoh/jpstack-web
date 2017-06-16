package wbs.smsapps.alerts.daemon;

import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.shorterThan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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
import wbs.utils.time.TimeFormatter;

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

	@ClassSingletonDependency
	LogContext logContext;

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

	@SingletonDependency
	TimeFormatter timeFormatter;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// details

	@Override
	protected
	String friendlyName () {
		return "Alerts settings sender";
	}

	@Override
	protected
	String backgroundProcessName () {
		return "alerts-settings.alert-sender";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			// work out current minute

			Instant currentMinute =
				transaction.now ()
					.toDateTime ()
					.withSecondOfMinute (0)
					.withMillisOfSecond (0)
					.toInstant ();

			// find alerts settings pending

			List <Long> alertsSettingsIds =
				alertsSettingsHelper.findAll (
					transaction)

				.stream ()

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
						transaction,
						alertsSettingsId);

				} catch (Exception exception) {

					transaction.errorFormatException (
						exception,
						"Error checking alerts for %s",
						integerToDecimalString (
							alertsSettingsId));

					exceptionLogger.logThrowable (
						transaction,
						"daemon",
						"alerts daemon " + alertsSettingsId,
						exception,
						optionalAbsent (),
						GenericExceptionResolution.ignoreWithNoWarning);

				}

			}

		}

	}

	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long alertsSettingsId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"runOnce");

		) {

			transaction.debugFormat (
				"checking if we should send an alert for %s",
				integerToDecimalString (
					alertsSettingsId));

			AlertsSettingsRec alertsSettings =
				alertsSettingsHelper.findRequired (
					transaction,
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

			Map <Record <?>, AlertsSubjectRec> subjects =
				new HashMap<> ();

			for (
				AlertsSubjectRec alertsSubject
					: alertsSettings.getAlertsSubjects ()
			) {

				Record <?> subject =
					objectManager.findObjectRequired (
						transaction,
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
					: queueSubjectHelper.findActive (
						transaction)
			) {

				if (
					! isSubject (
						transaction,
						subjects,
						queueSubject.getQueue ())
				) {

					continue;

				}

				for (
					QueueItemRec queueItem
						: queueLogic.getActiveQueueItems (
							transaction,
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

			transaction.debugFormat (
				"now is %s",
				timeFormatter.timestampSecondStringIso (
					now));

			transaction.debugFormat (
				"oldest is %s",
				timeFormatter.timestampSecondStringIso (
					oldest));

			transaction.debugFormat (
				"oldest duration is %s",
				timeFormatter.prettyDuration (
					maxDurationFoundInQueue));

			transaction.debugFormat (
				"unclaimed count is %s",
				integerToDecimalString (
					numUnclaimed));

			// check if alert is due

			boolean maxDurationExceeded =
				maxDurationFoundInQueue.isLongerThan (
					maxDurationAllowedInQueue);

			boolean maxItemsExceeded =
				numUnclaimed > alertsSettings.getMaxItemsInQueue ();

			boolean alertDue;

			if (
				! maxDurationExceeded
				&& ! maxItemsExceeded
			) {

				transaction.debugFormat (
					"everything within limits");

				alertDue = false;

			} else if (maxDurationExceeded) {

				transaction.debugFormat (
					"maximum duration of %s exceeded",
					maxDurationAllowedInQueue.toString ());

				alertDue = true;

			} else if (maxItemsExceeded) {

				transaction.debugFormat (
					"maximum unclaimed count of %s exceeded",
					integerToDecimalString (
						alertsSettings.getMaxItemsInQueue ()));

				alertDue = true;

			} else {

				throw new RuntimeException ();

			}

			// construct message

			TextRec messageText =
				constructMessage (
					transaction,
					alertsSettings,
					numUnclaimed,
					maxDurationFoundInQueue);

			// limit send frequency

			boolean sentRecently =
				checkSentRecently (
					transaction,
					alertsSettings);

			boolean active =
				checkActive (
					transaction,
					alertsSettings);

			boolean performSend =
				alertDue && ! sentRecently && active;

			if (performSend) {

				long numSent =
					sendAlerts (
						transaction,
						alertsSettings,
						messageText);

				// create alert

				alertsAlertHelper.insert (
					transaction,
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
				transaction,
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

	}

	private
	TextRec constructMessage (
			@NonNull Transaction parentTransaction,
			@NonNull AlertsSettingsRec alertsSettings,
			@NonNull Long numUnclaimed,
			@NonNull Duration maximumDuration) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"constructMessage");

		) {

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

			transaction.warningFormat (
				"%s",
				message);

			TextRec messageText =
				textHelper.findOrCreate (
					transaction,
					message);

			return messageText;

		}

	}

	private
	boolean checkSentRecently (
			@NonNull Transaction parentTransaction,
			@NonNull AlertsSettingsRec alertsSettings) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"checkSentRecently");

		) {

			Duration minAlertFrequency =
				new Duration (
					alertsSettings.getMaxAlertFrequency ()
					* 1000L);

			if (alertsSettings.getLastAlert () == null)
				return false;

			transaction.debugFormat (
				"last alert was at %s",
				timeFormatter.timestampSecondStringIso (
					alertsSettings.getLastAlert ()));

			Duration durationSinceLastSend =
				new Duration (
					alertsSettings.getLastAlert (),
					transaction.now ());

			transaction.debugFormat (
				"duration since last alert is %s",
				timeFormatter.prettyDuration (
					durationSinceLastSend));

			if (
				shorterThan (
					durationSinceLastSend,
					minAlertFrequency)
			) {

				transaction.debugFormat (
					"mininum alert frequency of %s not exceeded",
					timeFormatter.prettyDuration (
						minAlertFrequency));

				return true;

			} else {

				transaction.debugFormat (
					"minimum alert frequency of %s exceeded",
					timeFormatter.prettyDuration (
						minAlertFrequency));

				return false;

			}

		}

	}


	boolean checkActive (
			OwnedTransaction transaction,
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
			@NonNull Transaction parentTransaction,
			@NonNull Map <Record <?>, AlertsSubjectRec> subjects,
			@NonNull Record <?> object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"isSubject");

		) {

			Optional <Record <?>> parentOptional =
				objectManager.firstAncestor (
					transaction,
					object,
					subjects.keySet ());

			if (
				optionalIsNotPresent (
					parentOptional)
			) {
				return false;
			}

			Record <?> parent =
				optionalGetRequired (
					parentOptional);

			AlertsSubjectRec alertsSubject =
				subjects.get (
					parent);

			return alertsSubject.getInclude ();

		}

	}

	private
	int sendAlerts (
			@NonNull Transaction parentTransaction,
			@NonNull AlertsSettingsRec alertsSettings,
			@NonNull TextRec messageText) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendAlerts");

		) {

			// send alerts

			int numSent = 0;

			for (
				AlertsNumberRec alertsNumber
					: alertsSettings.getAlertsNumbers ()
			) {

				if (! alertsNumber.getEnabled ())
					continue;

				transaction.noticeFormat (
					"sending alert to %s",
					alertsNumber.getNumber ().getNumber ());

				ServiceRec alertsService =
					serviceHelper.findByCodeRequired (
						transaction,
						alertsSettings,
						"alerts");

				RouteRec route =
					routerLogic.resolveRouter (
						transaction,
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

					.send (
						transaction);

				numSent ++;

			}

			return numSent;

		}

	}

}
