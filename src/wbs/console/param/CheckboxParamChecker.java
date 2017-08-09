package wbs.console.param;

import static wbs.utils.string.StringUtils.stringEqualSafe;

public
class CheckboxParamChecker
	implements ParamChecker <Boolean> {

	String error;

	public
	CheckboxParamChecker (
			String error) {

		this.error = error;

	}

	@Override
	public
	Boolean get (
			String param) {

		if (param == null)
			return false;

		param =
			param.trim ();

		if (
			stringEqualSafe (
				param,
				"on")
		) {

			return true;

		}

		throw new ParamFormatException (
			error);

	}

}
