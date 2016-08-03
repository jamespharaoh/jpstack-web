/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.TimeUtils.instantToDateNullSafe;

import java.util.Date;

import org.joda.time.DateTimeZone;

import wbs.framework.utils.TextualInterval;
import wbs.framework.utils.etc.TimeFormatException;

@Deprecated
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

			// TODO timezone should not be hardcoded

			TextualInterval interval =
				TextualInterval.parseRequired (
					DateTimeZone.forID (
						"Europe/London"),
					param,
					0);

			return instantToDateNullSafe (
				interval.value ().getEnd ());

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}