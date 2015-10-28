package wbs.console.forms;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nameFormFieldConstraintValidator")
public
class NameFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	void validate (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue,
			@NonNull List<String> errors) {

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

	}

}
