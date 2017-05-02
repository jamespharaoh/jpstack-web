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
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"isNumberReportSuccessful");

		) {

			Instant sixMonthsAgo =
				transaction
					.now ()
					.minus (Duration.standardDays (365 / 2));

			Optional <ChatUserNumberReportRec> numberReportOptional =
				chatUserNumberReportHelper.find (
					transaction,
					number.getId ());

			if (
				optionalIsNotPresent (
					numberReportOptional)
			) {

				// no DR yet for this number

				transaction.debugFormat (
					"REPORT NULL %s",
					number.getNumber ());

				return true;

			}

			ChatUserNumberReportRec numberReport =
				numberReportOptional.get ();

			if (numberReport.getLastSuccess () != null) {

				transaction.debugFormat (
					"REPORT LAST SUCCESS %s %s",
					numberReport.getLastSuccess ().toString (),
					number.getNumber ());

				return laterThan (
					numberReport.getLastSuccess (),
					sixMonthsAgo);

			}

			if (numberReport.getFirstFailure () != null) {

				transaction.debugFormat (
					"REPORT FIRST FAILURE %s %s",
					numberReport.getFirstFailure ().toString (),
					number.getNumber ());

				return laterThan (
					numberReport.getFirstFailure (),
					sixMonthsAgo);

			}

			// shouldn't happen

			transaction.debugFormat (
				"REPORT ERROR %s",
				number.getNumber ());

			return true;

		}

	}

	@Override
	public
	boolean isNumberReportPastPermanentDeliveryConstraint (
			@NonNull Transaction parentTransaction,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"isNumberReportPastPermanentDeliveryConstraint");

		) {

			Optional <ChatUserNumberReportRec> numberReportOptional =
				chatUserNumberReportHelper.find (
					transaction,
					number.getId ());

			if (
				optionalIsNotPresent (
					numberReportOptional)
			) {

				// no DR yet for this number

				transaction.debugFormat (
					"REPORT PERMANENT NULL %s",
					number.getNumber ());

				return false;

			}

			ChatUserNumberReportRec numberReport =
				numberReportOptional.get ();

			if (numberReport.getPermanentFailureReceived () != null) {

				long count =
					numberReport.getPermanentFailureCount ();

				transaction.debugFormat (
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

}
