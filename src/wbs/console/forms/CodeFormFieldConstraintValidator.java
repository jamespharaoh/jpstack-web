package wbs.console.forms;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("codeFormFieldConstraintValidator")
public
class CodeFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	void validate (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue,
			@NonNull List<String> errors) {

		// TODO check code is not in use

	}

}
