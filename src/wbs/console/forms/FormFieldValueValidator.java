package wbs.console.forms;

import java.util.List;

import com.google.common.base.Optional;

public
interface FormFieldValueValidator<Generic> {

	void validate (
			Optional<Generic> genericValue,
			List<String> errors);

}
