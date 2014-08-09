package wbs.apn.chat.core.console;

import java.util.Calendar;
import java.util.Date;

public
class DayCounter {

	int dayNumber = 0;

	final Calendar calendar =
		Calendar.getInstance ();

	public
	boolean nextDate (
			Date newDate) {

		calendar.setTime (
			newDate);

		int newDayNumber =
			+ (calendar.get (Calendar.YEAR) << 9)
			+ calendar.get (Calendar.DAY_OF_YEAR);

		if (newDayNumber == dayNumber)
			return false;

		dayNumber = newDayNumber;

		return true;

	}

	public
	Date getDate () {
		return calendar.getTime ();
	}

}
