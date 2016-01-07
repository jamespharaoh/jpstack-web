package wbs.console.forms;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nullFormFieldValueValidator")
public
class NullFormFieldValueValidator<Generic>
	implements FormFieldValueValidator<Generic> {

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<Generic> genericValue) {

		return Optional.<String>absent ();

	}

}
