package wbs.console.param;

import static wbs.framework.utils.etc.StringUtils.stringEqual;
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
			stringEqual (
				param,
				"false")
		) {
			return false;
		}

		if (
			stringEqual (
				param,
				"true")
		) {
			return true;
		}

		throw new ParamFormatException (
			error);

	}

}
