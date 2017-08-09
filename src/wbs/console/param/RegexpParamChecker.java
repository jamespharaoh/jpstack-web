/**
 *
 */
package wbs.console.param;

import java.util.regex.Pattern;

public
class RegexpParamChecker
	implements ParamChecker<String> {

	private final
	String error;

	private final
	Pattern pattern;

	public
	RegexpParamChecker (
			String newError,
			Pattern newPattern) {

		error =
			newError;

		pattern =
			newPattern;

	}

	public
	RegexpParamChecker (
			String newError,
			String patternString) {

		this (
			newError,
			Pattern.compile (
				patternString));

	}

	@Override
	public
	String get (
			String param) {

		if (param == null)
			throw new ParamFormatException (error);

		if (! pattern.matcher(param).matches ())
			throw new ParamFormatException (error);

		return param;

	}

}
