package wbs.console.forms;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

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
