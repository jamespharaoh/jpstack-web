package wbs.console.param;

import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

@Deprecated
public
class YesNoParamChecker
	implements ParamChecker <Boolean> {

	private
	String error;

	private
	boolean required;

	public
	YesNoParamChecker (
			String error,
			boolean required) {

		this.error = error;
		this.required = required;

	}

	@Override
	public
	Boolean get (
			String param) {

		param =
			param.trim ();

		if (

			! required

			&& stringIsEmpty (
				param)

		) {
			return null;
		}

		if (
			stringEqualSafe (
				param,
				"false")
		) {
			return false;
		}

		if (
			stringEqualSafe (
				param,
				"true")
		) {
			return true;
		}

		throw new ParamFormatException (
			error);

	}

}
