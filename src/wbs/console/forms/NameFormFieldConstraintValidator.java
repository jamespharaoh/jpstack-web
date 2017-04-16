package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("nameFormFieldConstraintValidator")
public
class NameFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	Optional <String> validate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		/*

		TODO make this work

		if (codeChanged) {

			Record<?> existing =
				consoleHelper.findByCode (
					objectManager.getParentGlobalId (
						(Record<?>) object),
					newCode);

			if (existing != null) {

				requestContext.addError ("Name already in use");

				throw new InvalidFormValueException ();

			}

		}
		*/

		return Optional.<String>absent ();

	}

}
