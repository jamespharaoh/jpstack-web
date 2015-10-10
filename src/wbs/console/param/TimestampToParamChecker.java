/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.parseTimeBefore;

import java.util.Date;

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

			return instantToDate (
				parseTimeBefore (
					param));

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}