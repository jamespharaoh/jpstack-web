package wbs.console.forms;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.PrototypeComponent;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;

@PrototypeComponent ("requiredFormFieldValueValidator")
public
class RequiredFormFieldValueValidator<Generic>
	implements FormFieldValueValidator<Generic> {

	@Override
	public
	Optional<String> validate (
			Optional<Generic> genericValue) {

		if (
			optionalIsPresent (
				genericValue)
		) {

			return Optional.<String>absent ();

		} else {

			return Optional.of (
				stringFormat (
					"You must specify a value for this field"));

		}

	}

}
