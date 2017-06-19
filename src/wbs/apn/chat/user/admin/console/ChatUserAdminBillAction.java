package wbs.apn.chat.user.admin.console;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

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

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserBillLogObjectHelper;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserAdminBillResponder")
	ComponentProvider <WebResponder> billResponderProvider;

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

			return billResponderProvider.provide (
				taskLogger);

		}

	}

	// implementation

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			if (
				! requestContext.canContext (
					"chat.userCredit")
			) {

				requestContext.addError (
					"Access denied");

				return null;

			}

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// lock prevents race condition between limit check and update

			chatUserHelper.lock (
				transaction,
				chatUser);

			// enforce ��30/day limit

			if (
				chatCreditLogic.userBillLimitApplies (
					transaction,
					chatUser)
			) {

				requestContext.addError (
					"Daily limit reached");

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

			List <ChatUserBillLogRec> todayBillLogs =
				chatUserBillLogHelper.findByTimestamp (
					transaction,
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
				transaction,
				chatUser,
				true);

			// log it

			chatUserBillLogHelper.insert (
				transaction,
				chatUserBillLogHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

			);

			transaction.commit ();

			requestContext.addNotice (
				"User billed");

			return null;

		}

	}

}
