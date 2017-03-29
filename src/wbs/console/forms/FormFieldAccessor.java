package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

public
interface FormFieldAccessor <Container, Native> {

	Optional <Native> read (
			TaskLogger parentTaskLogger,
			Container container);

	void write (
			TaskLogger parentTaskLogger,
			Container container,
			Optional <Native> nativeValue);

}
