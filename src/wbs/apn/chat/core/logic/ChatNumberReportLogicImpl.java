package wbs.apn.chat.core.logic;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.number.core.model.NumberRec;

@Log4j
@SingletonComponent ("chatNumberReportLogic")
public
class ChatNumberReportLogicImpl
	implements ChatNumberReportLogic {

	@Inject
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@Override
	public
	boolean isNumberReportSuccessful (
			NumberRec number) {

		Calendar cal = new GregorianCalendar ();
		cal.setTime (new Date ());
		cal.add (Calendar.MONTH, -6);
		Date sixMonthsAgo = cal.getTime ();

		ChatUserNumberReportRec numberReportRec =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (numberReportRec == null) {
			// no DR yet for this number
			log.debug ("REPORT NULL " + number.getNumber ());
			return true;
		}

		if (numberReportRec.getLastSuccess () != null) {
			log.debug ("REPORT LAST SUCCESS "
					+ numberReportRec.getLastSuccess () + " "
					+ number.getNumber ());
			return numberReportRec.getLastSuccess ().after (sixMonthsAgo);
		}

		if (numberReportRec.getFirstFailure () != null) {
			log.debug ("REPORT FIRST FAILURE "
					+ numberReportRec.getFirstFailure () + " "
					+ number.getNumber ());
			return numberReportRec.getFirstFailure ().after (sixMonthsAgo);
		}

		// shouldn't happen
		log.debug ("REPORT ERROR " + number.getNumber ());
		return true;

	}

	@Override
	public boolean isNumberReportPastPermanentDeliveryConstraint (
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

			int count = numberReportRec.getPermanentFailureCount ();

			log.debug ("REPORT PERMANENT COUNT " + count + " "
					+ number.getNumber ());

			// disabled at sam's request
			// if (count >= 42)
			// return true;

		}

		return false;

	}

}
