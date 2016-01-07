package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldConstraintValidator<Container,Native> {

	Optional<String> validate (
			Container container,
			Optional<Native> nativeValue);

}
