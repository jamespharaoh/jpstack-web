package wbs.apn.chat.user.admin.console;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserBillLogObjectHelper;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserAdminBillAction")
public
class ChatUserAdminBillAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatUserBillLogObjectHelper chatUserBillLogHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatUserAdminBillResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		if (
			! requestContext.canContext (
				"chat.userCredit")
		) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatUserAdminBillAction.goReal ()",
					this);

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired ();

			// lock prevents race condition between limit check and update

			chatUserHelper.lock (
				chatUser);

			// enforce ��30/day limit

			if (chatCreditLogic.userBillLimitApplies (chatUser)) {

				requestContext.addError ("Daily limit reached");

				return null;

			}

			// enforce three per day limit

			LocalDate today =
				LocalDate.now ();

			Instant startOfToday =
				today
					.toDateTimeAtStartOfDay ()
					.toInstant ();

			Instant endOfToday =
				today
					.plusDays (1)
					.toDateTimeAtStartOfDay ()
					.toInstant ();

			List<ChatUserBillLogRec> todayBillLogs =
				chatUserBillLogHelper.findByTimestamp (
					chatUser,
					new Interval (
						startOfToday,
						endOfToday));

			boolean dailyAdminBillLimitReached =
				todayBillLogs.size () > 3;

			boolean canBypassDailyAdminBillLimit =
				requestContext.canContext ("chat.manage");

			if (
				dailyAdminBillLimitReached
				&& ! canBypassDailyAdminBillLimit
			) {

				requestContext.addError (
					"Daily admin bill limit reached");

			}

			// bill the user

			chatCreditLogic.userBillReal (
				taskLogger,
				chatUser,
				true);

			// log it

			chatUserBillLogHelper.insert (
				taskLogger,
				chatUserBillLogHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setUser (
					userConsoleLogic.userRequired ())

			);

			transaction.commit ();

			requestContext.addNotice (
				"User billed");

			return null;

		}

	}

}
