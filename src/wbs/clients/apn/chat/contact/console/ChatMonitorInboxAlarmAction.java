package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Seconds;

import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TextualInterval;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.TimeFormatException;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatMonitorInboxAlarmAction")
public
class ChatMonitorInboxAlarmAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@Inject
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// state

	DateTimeZone timeZone;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatMonitorInboxSummaryResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal ()
			throws ServletException {

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatMonitorInboxAlarmAction.goReal ()",
				this);

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.findRequired (
				requestContext.stuffInt (
					"chatMonitorInboxId"));

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
				userChatUser,
				monitorChatUser);

		timeZone =
			DateTimeZone.forID (
				chatScheme.getTimezone ());

		// pull in parameters

		boolean save =
			isPresent (
				requestContext.parameter (
					"alarmSet"));

		boolean clear =
			isPresent (
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
						0);

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
				chatUserAlarm);

			// and create log

			chatUserInitiationLogHelper.insert (
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
					userConsoleLogic.userRequired ())

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
					chatUserAlarm);

			}

			// create log

			chatUserInitiationLogHelper.insert (
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
					userConsoleLogic.userRequired ())

			);

		}

		transaction.commit ();

		return null;

	}

}
