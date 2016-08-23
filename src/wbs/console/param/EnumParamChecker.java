package wbs.console.param;

import static wbs.framework.utils.etc.LogicUtils.not;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import lombok.NonNull;

public
class EnumParamChecker <EnumType extends Enum <EnumType>>
	implements ParamChecker <EnumType> {

	String error;
	Class<EnumType> enumClass;
	boolean required;

	public
	EnumParamChecker (
			String newError,
			boolean required,
			Class<EnumType> enumClass) {

		error = newError;
		this.required = required;
		this.enumClass = enumClass;

	}

	@Override
	public
	EnumType get (
			@NonNull String param) {

		param =
			param.trim ();

		if (

			stringIsEmpty (
				param)

			&& not (
				required)

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