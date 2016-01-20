package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("requiredFormFieldValueValidator")
public
class RequiredFormFieldValueValidator<Generic>
	implements FormFieldValueValidator<Generic> {

	@Override
	public
	Optional<String> validate (
			Optional<Generic> genericValue) {

		if (
			isPresent (
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
