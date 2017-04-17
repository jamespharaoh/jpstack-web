/**
 *
 */
package wbs.console.param;

import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;

import java.util.Date;

import org.joda.time.DateTimeZone;

import wbs.utils.time.TextualInterval;
import wbs.utils.time.TimeFormatException;

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
			String originalParam) {

		String param =
			originalParam != null
				? originalParam.trim ()
				: "";

		if (

			! required

			&& stringIsEmpty (
				param)

		) {
			return null;
		}

		try {

			// TODO timestamp should not be hardcoded

			TextualInterval interval =
				TextualInterval.parseRequired (
					DateTimeZone.forID (
						"Europe/London"),
					param,
					0l);

			return instantToDateNullSafe (
				interval.start ());

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}