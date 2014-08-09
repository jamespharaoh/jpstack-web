package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("integerFormFieldValueValidator")
public
class IntegerFormFieldValueValidator
	implements FormFieldValueValidator<Integer> {

	@Getter @Setter
	String label;

	@Getter @Setter
	Integer minimum;

	@Getter @Setter
	Integer maximum;

	@Override
	public
	void validate (
			Integer genericValue,
			List<String> errors) {

		if (genericValue == null)
			return;

		if (genericValue < minimum
				|| genericValue > maximum) {

			errors.add (
				stringFormat (
					"Value for \"%s\" must be between %s and %s",
					label (),
					minimum,
					maximum));

		}

	}

}
