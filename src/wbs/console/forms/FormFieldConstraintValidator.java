package wbs.console.forms;

import java.util.List;

public
interface FormFieldConstraintValidator<Container,Native> {

	void validate (
			Container container,
			Native nativeValue,
			List<String> errors);

}
