package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldConstraintValidator")
public
class ObjectFormFieldConstraintValidator <Container, Generic>
	implements FormFieldConstraintValidator <Container, Generic> {

	@Override
	public
	Optional <String> validate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <Generic> nativeValue) {

		return optionalAbsent ();

	}

}
