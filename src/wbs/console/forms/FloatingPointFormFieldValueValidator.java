package wbs.console.forms;

import static wbs.utils.etc.NumberUtils.floatToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("doubleFormFieldValueValidator")
public
class FloatingPointFormFieldValueValidator
	implements FormFieldValueValidator <Double> {

	@Getter @Setter
	String label;

	@Getter @Setter
	Double minimum;

	@Getter @Setter
	Double maximum;

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <Double> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return optionalAbsent ();

		}

		if (
			genericValue.get () < minimum
			|| genericValue.get () > maximum
		) {

			return optionalOf (			
				stringFormat (
					"Value for \"%s\" must be between %s and %s",
					label (),
					floatToDecimalString (
						minimum),
					floatToDecimalString (
						maximum)));

		}

		return optionalAbsent ();

	}

}
