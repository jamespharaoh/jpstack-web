/**
 *
 */
package wbs.console.param;

import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public
class IntegerParamChecker
	implements ParamChecker <Long> {

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
	Long get (
			String param) {

		param =
			param.trim ();

		if (

			not (
				required)

			&& stringIsEmpty (
				param)

		) {

			return null;

		}

		Matcher matcher =
			pattern.matcher (param);

		if (! matcher.matches ())
			throw new ParamFormatException (error);

		return Long.parseLong (param);

	}

}