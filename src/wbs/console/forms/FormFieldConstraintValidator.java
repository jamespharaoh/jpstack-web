package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

public
interface FormFieldConstraintValidator <Container, Native> {

	Optional <String> validate (
			TaskLogger parentTaskLogger,
			Container container,
			Optional <Native> nativeValue);

}
