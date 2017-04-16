package wbs.apn.chat.core.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.time.TimeUtils.laterThan;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("chatNumberReportLogic")
public
class ChatNumberReportLogicImplementation
	implements ChatNumberReportLogic {

	// singleton dependencies

	@SingletonDependency
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	boolean isNumberReportSuccessful (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull NumberRec number) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"isNumberReportSuccessful");

		Transaction transaction =
			database.currentTransaction ();

		Instant sixMonthsAgo =
			transaction
				.now ()
				.minus (Duration.standardDays (365 / 2));

		Optional<ChatUserNumberReportRec> numberReportOptional =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (
			optionalIsNotPresent (
				numberReportOptional)
		) {

			// no DR yet for this number

			taskLogger.debugFormat (
				"REPORT NULL %s",
				number.getNumber ());

			return true;

		}

		ChatUserNumberReportRec numberReport =
			numberReportOptional.get ();

		if (numberReport.getLastSuccess () != null) {

			taskLogger.debugFormat (
				"REPORT LAST SUCCESS %s %s",
				numberReport.getLastSuccess ().toString (),
				number.getNumber ());

			return laterThan (
				numberReport.getLastSuccess (),
				sixMonthsAgo);

		}

		if (numberReport.getFirstFailure () != null) {

			taskLogger.debugFormat (
				"REPORT FIRST FAILURE %s %s",
				numberReport.getFirstFailure ().toString (),
				number.getNumber ());

			return laterThan (
				numberReport.getFirstFailure (),
				sixMonthsAgo);

		}

		// shouldn't happen

		taskLogger.debugFormat (
			"REPORT ERROR %s",
			number.getNumber ());

		return true;

	}

	@Override
	public
	boolean isNumberReportPastPermanentDeliveryConstraint (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull NumberRec number) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"isNumberReportPastPermanentDeliveryConstraint");

		Optional<ChatUserNumberReportRec> numberReportOptional =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (
			optionalIsNotPresent (
				numberReportOptional)
		) {

			// no DR yet for this number

			taskLogger.debugFormat (
				"REPORT PERMANENT NULL %s",
				number.getNumber ());

			return false;

		}

		ChatUserNumberReportRec numberReport =
			numberReportOptional.get ();

		if (numberReport.getPermanentFailureReceived () != null) {

			long count =
				numberReport.getPermanentFailureCount ();

			taskLogger.debugFormat (
				"REPORT PERMANENT COUNT %s %s",
				integerToDecimalString (
					count),
				number.getNumber ());

			// disabled at sam's request
			// if (count >= 42)
			// return true;

		}

		return false;

	}

}
