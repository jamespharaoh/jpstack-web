package wbs.console.forms;

import java.util.List;

public
interface FormFieldValueValidator<Generic> {

	void validate (
			Generic genericValue,
			List<String> errors);

}
