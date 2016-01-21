/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Date;

import org.joda.time.DateTimeZone;

import wbs.framework.utils.TextualInterval;
import wbs.framework.utils.etc.TimeFormatException;

@Deprecated
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

			TextualInterval interval =
				TextualInterval.parseRequired (
					DateTimeZone.forID (
						"Europe/London"),
					param,
					0);

			return instantToDate (
				interval.value ().getStart ());

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}