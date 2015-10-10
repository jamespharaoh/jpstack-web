/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.parseTimeAfter;

import java.util.Date;

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

			return instantToDate (
				parseTimeAfter (
					param));

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}