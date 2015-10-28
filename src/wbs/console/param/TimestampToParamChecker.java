/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.parsePartialTimestamp;

import java.util.Date;

import org.joda.time.Interval;

import wbs.framework.utils.etc.TimeFormatException;

public
class TimestampToParamChecker
	implements ParamChecker<Date> {

	private final
	String error;

	private final
	boolean required;

	public
	TimestampToParamChecker (
			String newError,
			boolean required) {

		error =
			newError;

		this.required =
			required;

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

			Interval interval =
				parsePartialTimestamp (
					param);

			return instantToDate (
				interval.getEnd ());

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}