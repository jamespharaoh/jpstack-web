package wbs.clients.apn.chat.core.logic;

import static wbs.framework.utils.etc.Misc.laterThan;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

		ChatUserNumberReportRec numberReportRec =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (numberReportRec == null) {

			// no DR yet for this number

			log.debug (
				"REPORT NULL " + number.getNumber ());

			return true;

		}

		if (numberReportRec.getLastSuccess () != null) {

			log.debug (
				stringFormat (
					"REPORT LAST SUCCESS %s %s",
					numberReportRec.getLastSuccess (),
					number.getNumber ()));

			return laterThan (
				numberReportRec.getLastSuccess (),
				sixMonthsAgo);

		}

		if (numberReportRec.getFirstFailure () != null) {

			log.debug (
				stringFormat (
					"REPORT FIRST FAILURE %s %s",
					numberReportRec.getFirstFailure (),
					number.getNumber ()));

			return laterThan (
				numberReportRec.getFirstFailure (),
				sixMonthsAgo);

		}

		// shouldn't happen
		log.debug ("REPORT ERROR " + number.getNumber ());
		return true;

	}

	@Override
	public
	boolean isNumberReportPastPermanentDeliveryConstraint (
			NumberRec number) {

		ChatUserNumberReportRec numberReportRec =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (numberReportRec == null) {

			// no DR yet for this number

			log.debug ("REPORT PERMANENT NULL " + number.getNumber ());

			return false;

		}

		if (numberReportRec.getPermanentFailureReceived () != null) {

			long count =
				numberReportRec.getPermanentFailureCount ();

			log.debug ("REPORT PERMANENT COUNT " + count + " "
					+ number.getNumber ());

			// disabled at sam's request
			// if (count >= 42)
			// return true;

		}

		return false;

	}

}
