package wbs.console.forms;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

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
