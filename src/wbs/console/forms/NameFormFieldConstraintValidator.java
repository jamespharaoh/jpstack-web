package wbs.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nameFormFieldConstraintValidator")
public
class NameFormFieldConstraintValidator<Container>
	implements FormFieldConstraintValidator<Container,String> {

	@Override
	public
	void validate (
			Container container,
			String nativeValue,
			List<String> errors) {

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
