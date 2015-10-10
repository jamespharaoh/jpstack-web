package wbs.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("codeFormFieldConstraintValidator")
public
class CodeFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	void validate (
			Container container,
			String nativeValue,
			List<String> errors) {

		// TODO check code is not in use

	}

}
