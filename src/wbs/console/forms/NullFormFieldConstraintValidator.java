package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nullFormFieldConstraintValidator")
public
class NullFormFieldConstraintValidator<Container,Native>
	implements FormFieldConstraintValidator<Container,Native> {

	@Override
	public
	void validate (
			Container container,
			Native nativeValue,
			List<String> errors) {

		doNothing ();

	}

}
