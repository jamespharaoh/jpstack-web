package wbs.platform.console.forms;

import java.util.List;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldConstraintValidator")
public
class ObjectFormFieldConstraintValidator<
	Container extends Record<?>,
	Generic extends Record<?>
>
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
