package wbs.console.param;

import static wbs.framework.utils.etc.StringUtils.stringEqual;

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
			stringEqual (
				param,
				"on")
		) {

			return true;

		}

		throw new ParamFormatException (
			error);

	}

}
