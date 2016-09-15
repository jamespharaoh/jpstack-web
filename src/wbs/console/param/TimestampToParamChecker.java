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

		if (

			! required

			&& stringIsEmpty (
				param)

		) {
			return null;
		}

		try {

			// TODO timezone should not be hardcoded

			TextualInterval interval =
				TextualInterval.parseRequired (
					DateTimeZone.forID (
						"Europe/London"),
					param,
					0l);

			return instantToDateNullSafe (
				interval.value ().getEnd ());

		} catch (TimeFormatException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}