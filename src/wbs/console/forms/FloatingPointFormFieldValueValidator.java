package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("doubleFormFieldValueValidator")
public
class FloatingPointFormFieldValueValidator
	implements FormFieldValueValidator<Double> {

	@Getter @Setter
	String label;

	@Getter @Setter
	Double minimum;

	@Getter @Setter
	Double maximum;

	@Override
	public
	void validate (
			Double genericValue,
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
