package wbs.console.forms.basic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("integerFormFieldValueValidator")
public
class IntegerFormFieldValueValidator
	implements FormFieldValueValidator <Long> {

	@Getter @Setter
	String label;

	@Getter @Setter
	Long minimum;

	@Getter @Setter
	Long maximum;

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <Long> genericValue) {

		if (! genericValue.isPresent ()) {

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
					integerToDecimalString (
						minimum),
					integerToDecimalString (
						maximum)));

		}

		return optionalAbsent ();

	}

}
