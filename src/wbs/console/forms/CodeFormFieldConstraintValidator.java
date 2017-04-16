package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("codeFormFieldConstraintValidator")
public
class CodeFormFieldConstraintValidator <Container>
	implements FormFieldConstraintValidator <Container, String> {

	@Override
	public
	Optional <String> validate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		return optionalAbsent ();

	}

}
