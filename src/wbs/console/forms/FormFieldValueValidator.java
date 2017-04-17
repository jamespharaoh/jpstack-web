package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldValueValidator <Generic> {

	Optional <String> validate (
			Optional <Generic> genericValue);

}
