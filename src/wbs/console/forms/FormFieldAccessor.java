package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldAccessor<Container,Native> {

	Optional<Native> read (
			Container container);

	void write (
			Container container,
			Optional<Native> nativeValue);

}
