package wbs.console.forms;

import java.util.List;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldConstraintValidator")
public
class ObjectFormFieldConstraintValidator<Container,Generic>
	implements FormFieldConstraintValidator<Container,Generic> {

	@Override
	public
	void validate (
			Container container,
			Generic nativeValue,
			List<String> errors) {

		// TODO validate the root and permissions

	}

}
