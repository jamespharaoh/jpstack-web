package wbs.console.forms;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("codeFormFieldConstraintValidator")
public
class CodeFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	Optional<String> validate (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue) {

		return Optional.<String>absent ();

	}

}
