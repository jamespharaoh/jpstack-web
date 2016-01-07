package wbs.console.forms;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nameFormFieldConstraintValidator")
public
class NameFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	Optional<String> validate (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue) {

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
