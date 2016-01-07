package wbs.console.forms;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nullFormFieldConstraintValidator")
public
class NullFormFieldConstraintValidator<Container,Native>
	implements FormFieldConstraintValidator<Container,Native> {

	@Override
	public
	Optional<String> validate (
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue) {

		return Optional.<String>absent ();

	}

}
