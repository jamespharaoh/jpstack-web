/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public
class FixedParamChecker
	implements ParamChecker<Long> {

	String error;
	int decimalPlaces;
	boolean required;
	Pattern pattern;

	public
	FixedParamChecker (
			String newError,
			boolean required,
			int newDecimalPlaces) {

		error =
			newError;

		decimalPlaces =
			newDecimalPlaces;

		this.required =
			required;

		if (decimalPlaces < 0)
			throw new IllegalArgumentException ();

		pattern =
			decimalPlaces == 0
				? Pattern.compile ("-?\\d+")
				: Pattern.compile (
					String.format (
						"-?(\\d+)\\.(\\d{%d})",
						decimalPlaces));

	}

	@Override
	public
	Long get (
			String param) {

		if (decimalPlaces == 0) {

			try {

				return Long.parseLong (
					param);

			} catch (NumberFormatException exception) {

				throw new ParamFormatException (
					error);

			}

		}

		if (equal ("", param.trim ()) && ! required)
			return null;

		Matcher matcher =
			pattern.matcher (param);

		if (! matcher.matches ()) {

			throw new ParamFormatException (
				error);

		}

		long value =
			- Long.parseLong (
				matcher.group (1));

		for (int i = 0; i < decimalPlaces; i++)
			value *= 10;

		value -=
			Integer.parseInt (
				matcher.group (2));

		if (param.charAt (0) != '-')
			value = -value;

		return value;

	}

}
