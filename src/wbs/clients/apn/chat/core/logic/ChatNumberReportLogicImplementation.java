package wbs.clients.apn.chat.core.logic;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.TimeUtils.laterThan;

import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.number.core.model.NumberRec;

@Log4j
@SingletonComponent ("chatNumberReportLogic")
public
class ChatNumberReportLogicImplementation
	implements ChatNumberReportLogic {

	// dependencies

	@Inject
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@Inject
	Database database;

	// implementation

	@Override
	public
	boolean isNumberReportSuccessful (
			@NonNull NumberRec number) {

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

			log.debug (
				"REPORT NULL " + number.getNumber ());

			return true;

		}

		ChatUserNumberReportRec numberReport =
			numberReportOptional.get ();

		if (numberReport.getLastSuccess () != null) {

			log.debug (
				stringFormat (
					"REPORT LAST SUCCESS %s %s",
					numberReport.getLastSuccess (),
					number.getNumber ()));

			return laterThan (
				numberReport.getLastSuccess (),
				sixMonthsAgo);

		}

		if (numberReport.getFirstFailure () != null) {

			log.debug (
				stringFormat (
					"REPORT FIRST FAILURE %s %s",
					numberReport.getFirstFailure (),
					number.getNumber ()));

			return laterThan (
				numberReport.getFirstFailure (),
				sixMonthsAgo);

		}

		// shouldn't happen

		log.debug (
			"REPORT ERROR " + number.getNumber ());

		return true;

	}

	@Override
	public
	boolean isNumberReportPastPermanentDeliveryConstraint (
			@NonNull NumberRec number) {

		Optional<ChatUserNumberReportRec> numberReportOptional =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (
			optionalIsNotPresent (
				numberReportOptional)
		) {

			// no DR yet for this number

			log.debug ("REPORT PERMANENT NULL " + number.getNumber ());

			return false;

		}

		ChatUserNumberReportRec numberReport =
			numberReportOptional.get ();

		if (numberReport.getPermanentFailureReceived () != null) {

			long count =
				numberReport.getPermanentFailureCount ();

			log.debug ("REPORT PERMANENT COUNT " + count + " "
					+ number.getNumber ());

			// disabled at sam's request
			// if (count >= 42)
			// return true;

		}

		return false;

	}

}
