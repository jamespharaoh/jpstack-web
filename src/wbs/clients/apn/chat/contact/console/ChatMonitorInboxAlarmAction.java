package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.parseTimeAfter;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import org.joda.time.Instant;
import org.joda.time.Seconds;

import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.TimeFormatException;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatMonitorInboxAlarmAction")
public
class ChatMonitorInboxAlarmAction
	extends ConsoleAction {

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
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {

		return responder ("chatMonitorInboxSummaryResponder");

	}

	@Override
	protected
	Responder goReal ()
			throws ServletException {

		boolean save =
			requestContext.parameter ("alarmSet") != null;

		boolean clear =
			requestContext.parameter ("alarmCancel") != null;

		boolean sticky =
			Boolean.parseBoolean (
				requestContext.parameter ("alarmSticky"));

		Instant alarmTime = null;

		if (save) {

			// parse alarm time from form

			try {

				// TODO this is wrong, surely?
				alarmTime =
					parseTimeAfter (
						stringFormat (
							"%s %s",
							requestContext.parameter ("alarmDate"),
							requestContext.parameter ("alarmTime")));

			} catch (TimeFormatException exception) {

				requestContext.addError ("Alarm time invalid");

				return null;
			}

			// ensure time is not in past

			if (alarmTime.isBefore (Instant.now ())) {

				requestContext.addError ("Alarm time is in past");

				return null;
			}

		}

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.find (
				requestContext .stuffInt ("chatMonitorInboxId"));

		ChatUserRec userChatUser =
			chatMonitorInbox.getUserChatUser ();

		ChatUserRec monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		ChatRec chat =
			userChatUser.getChat ();

		ChatUserAlarmRec chatUserAlarm =
			chatUserAlarmHelper.find (
				userChatUser,
				monitorChatUser);

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
						requestContext.prettySecsInterval (
							chat.getMaxAlarmTime ())));

				return null;

			}

		}

		// remove existing alarm if required

		if (chatUserAlarm != null && (clear || alarmTime == null)) {

			chatUserAlarmHelper.remove (
				chatUserAlarm);

			// and create log

			chatUserInitiationLogHelper.insert (
				new ChatUserInitiationLogRec ()
					.setChatUser (userChatUser)
					.setMonitorChatUser (monitorChatUser)
					.setReason (ChatUserInitiationReason.alarmCancel)
					.setTimestamp (transaction.now ().toDate ())
					.setMonitorUser (myUser)
					.setAlarmTime (chatUserAlarm.getAlarmTime ()));

		}

		if (save && alarmTime != null) {

			// create alarm if required

			boolean insert = false;

			if (chatUserAlarm == null) {

				chatUserAlarm =
					new ChatUserAlarmRec ()
						.setChatUser (userChatUser)
						.setMonitorChatUser (monitorChatUser);

				insert = true;

			}

			// and update it

			chatUserAlarm
				.setAlarmTime (alarmTime.toDate ())
				.setResetTime (
					transaction.now ()
						.toDateTime ()
						.plusHours (1)
						.toInstant ()
						.toDate ())
				.setSticky (sticky);

			// insert it if needed

			if (insert) {

				chatUserAlarmHelper.insert (
					chatUserAlarm);

			}

			// create log

			chatUserInitiationLogHelper.insert (
				new ChatUserInitiationLogRec ()
					.setChatUser (userChatUser)
					.setMonitorChatUser (monitorChatUser)
					.setReason (ChatUserInitiationReason.alarmSet)
					.setAlarmTime (alarmTime.toDate ())
					.setTimestamp (transaction.now ().toDate ())
					.setMonitorUser (myUser));

		}

		transaction.commit ();

		return null;

	}

}
