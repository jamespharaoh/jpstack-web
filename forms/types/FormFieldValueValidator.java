package wbs.console.forms.types;

import com.google.common.base.Optional;

public
interface FormFieldValueValidator <Generic> {

	Optional <String> validate (
			Optional <Generic> genericValue);

}
