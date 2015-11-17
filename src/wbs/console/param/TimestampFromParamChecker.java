/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.parsePartialTimestamp;

import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import wbs.framework.utils.etc.TimeFormatException;

public
class TimestampFromParamChecker
	implements ParamChecker<Date> {

	String error;
	boolean required;

	public
	TimestampFromParamChecker (
			String error,
			boolean required) {

		this.error = error;
		this.required = required;

	}

	@Override
	public
	Date get (
			String param) {

		param =
			param != null
				? param.trim ()
				: "";

		if (equal ("", param) && ! required)
			return null;

		try {

			// TODO timestamp should not be hardcoded

			Interval interval =
				parsePartialTimestamp (
					DateTimeZone.forID (
						"Europe/London"),
					param);

			return instantToDate (
				interval.getStart ());

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}