package wbs.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nullFormFieldValueValidator")
public
class NullFormFieldValueValidator<Generic>
	implements FormFieldValueValidator<Generic> {

	@Override
	public
	void validate (
			Generic genericValue,
			List<String> errors) {

	}

}
