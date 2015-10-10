package wbs.console.param;

import static wbs.framework.utils.etc.Misc.equal;

public
class EnumParamChecker<E extends Enum<E>>
	implements ParamChecker<E> {

	String error;
	Class<E> enumClass;
	boolean required;

	public
	EnumParamChecker (
			String newError,
			boolean required,
			Class<E> enumClass) {

		error = newError;
		this.required = required;
		this.enumClass = enumClass;

	}

	@Override
	public
	E get (
			String param) {

		param =
			param.trim ();

		if (
			equal (
				"",
				param)
			&& ! required
		) {

			return null;

		}

		try {

			return Enum.valueOf (
				enumClass,
				param);

		} catch (IllegalArgumentException exception) {

			throw new ParamFormatException (
				error);

		}

	}

}