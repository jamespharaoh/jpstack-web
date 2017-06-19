package wbs.apn.chat.contact.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Seconds;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.time.TextualInterval;
import wbs.utils.time.TimeFormatException;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.console.ChatUserAlarmConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatMonitorInboxAlarmAction")
public
class ChatMonitorInboxAlarmAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatMonitorInboxConsoleHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatUserAlarmConsoleHelper chatUserAlarmHelper;

	@SingletonDependency
	ChatUserInitiationLogConsoleHelper chatUserInitiationLogHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatMonitorInboxSummaryResponder")
	ComponentProvider <WebResponder> summaryResponderProvider;

	// state

	DateTimeZone timeZone;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return summaryResponderProvider.provide (
				taskLogger);

		}

	}

	// implementation

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			ChatMonitorInboxRec chatMonitorInbox =
				chatMonitorInboxHelper.findFromContextRequired (
					transaction);

			ChatUserRec userChatUser =
				chatMonitorInbox.getUserChatUser ();

			ChatUserRec monitorChatUser =
				chatMonitorInbox.getMonitorChatUser ();

			ChatRec chat =
				userChatUser.getChat ();

			ChatSchemeRec chatScheme =
				userChatUser.getChatScheme ();

			ChatUserAlarmRec chatUserAlarm =
				chatUserAlarmHelper.find (
					transaction,
					userChatUser,
					monitorChatUser);

			timeZone =
				DateTimeZone.forID (
					chatScheme.getTimezone ());

			// pull in parameters

			boolean save =
				optionalIsPresent (
					requestContext.parameter (
						"alarmSet"));

			boolean clear =
				optionalIsPresent (
					requestContext.parameter (
						"alarmCancel"));

			boolean sticky =
				Boolean.parseBoolean (
					requestContext.parameterRequired (
						"alarmSticky"));

			Instant alarmTime = null;

			if (save) {

				// parse alarm time from form

				try {

					String timestampString =
						stringFormat (
							"%s %s",
							requestContext.parameterRequired (
								"alarmDate"),
							requestContext.parameterRequired (
								"alarmTime"));

					TextualInterval interval =
						TextualInterval.parseRequired (
							timeZone,
							timestampString,
							0l);

					alarmTime =
						interval.start ();

				} catch (TimeFormatException exception) {

					requestContext.addError (
						"Alarm time invalid");

					return null;
				}

				// ensure time is not in past

				if (alarmTime.isBefore (Instant.now ())) {

					requestContext.addError (
						"Alarm time is in past");

					return null;
				}

			}

			// check alarm is within allowed range

			if (save) {

				Seconds secondsTillAlarm =
					Seconds.secondsBetween (
						transaction.now (),
						alarmTime);

				if (secondsTillAlarm.getSeconds () > chat.getMaxAlarmTime ()) {

					requestContext.addError (
						stringFormat (
							"Alarm time exceeds maximum of %s",
							timeFormatter.prettyDuration (
								Duration.standardSeconds (
									chat.getMaxAlarmTime ()))));

					return null;

				}

			}

			// remove existing alarm if required

			if (chatUserAlarm != null && (clear || alarmTime == null)) {

				chatUserAlarmHelper.remove (
					transaction,
					chatUserAlarm);

				// and create log

				chatUserInitiationLogHelper.insert (
					transaction,
					chatUserInitiationLogHelper.createInstance ()

					.setChatUser (
						userChatUser)

					.setMonitorChatUser (
						monitorChatUser)

					.setReason (
						ChatUserInitiationReason.alarmCancel)

					.setTimestamp (
						transaction.now ())

					.setMonitorUser (
						userConsoleLogic.userRequired (
							transaction))

					.setAlarmTime (
						chatUserAlarm.getAlarmTime ())

				);

			}

			if (save && alarmTime != null) {

				// create alarm if required

				boolean insert = false;

				if (chatUserAlarm == null) {

					chatUserAlarm =
						chatUserAlarmHelper.createInstance ()

						.setChatUser (
							userChatUser)

						.setMonitorChatUser (
							monitorChatUser);

					insert = true;

				}

				// and update it

				chatUserAlarm

					.setAlarmTime (
						alarmTime)

					.setResetTime (
						transaction.now ().plus (
							Duration.standardHours (1)))

					.setSticky (
						sticky);

				// insert it if needed

				if (insert) {

					chatUserAlarmHelper.insert (
						transaction,
						chatUserAlarm);

				}

				// create log

				chatUserInitiationLogHelper.insert (
					transaction,
					chatUserInitiationLogHelper.createInstance ()

					.setChatUser (
						userChatUser)

					.setMonitorChatUser (
						monitorChatUser)

					.setReason (
						ChatUserInitiationReason.alarmSet)

					.setAlarmTime (
						alarmTime)

					.setTimestamp (
						transaction.now ())

					.setMonitorUser (
						userConsoleLogic.userRequired (
							transaction))

				);

			}

			transaction.commit ();

			return null;

		}

	}

}
