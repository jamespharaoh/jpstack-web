/**
 *
 */
package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public
class IntegerParamChecker
	implements ParamChecker<Integer> {

	String error;
	boolean required;

	Pattern pattern =
		Pattern.compile (
			"\\d+");

	public
	IntegerParamChecker (
			String error,
			boolean required) {

		this.error = error;
		this.required = required;

	}

	@Override
	public
	Integer get (
			String param) {

		param =
			param.trim ();

		if (equal ("", param) && ! required)
			return null;

		Matcher matcher =
			pattern.matcher (param);

		if (! matcher.matches ())
			throw new ParamFormatException (error);

		return Integer.parseInt (param);

	}

}