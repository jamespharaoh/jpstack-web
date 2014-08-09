package wbs.framework.database;

public
class SimpleDate {

	public final
	int year, month, day;

	public
	SimpleDate (
			int newYear,
			int newMonth,
			int newDay) {

		year = newYear;
		month = newMonth;
		day = newDay;

	}

	public
	boolean equals (
			SimpleDate other) {

		return year == other.year
			&& month == other.month
			&& day == other.day;

	}

	@Override
	public
	boolean equals (
			Object object) {

		if (! (object instanceof SimpleDate))
			return false;

		return equals (
			(SimpleDate) object);

	}

	@Override
	public
	int hashCode () {

		return
			+ year * 12 * 31
			+ month * 31
			+ day;

	}

}
