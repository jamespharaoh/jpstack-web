package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;

public
class YesNoParamChecker
	implements ParamChecker<Boolean> {

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

		if (equal (param, "") && ! required)
			return null;

		if (equal (param, "false"))
			return false;

		if (equal (param, "true"))
			return true;

		throw new ParamFormatException (
			error);

	}

}
