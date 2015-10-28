package wbs.console.forms;

import java.util.List;

import com.google.common.base.Optional;

public
interface FormFieldConstraintValidator<Container,Native> {

	void validate (
			Container container,
			Optional<Native> nativeValue,
			List<String> errors);

}
