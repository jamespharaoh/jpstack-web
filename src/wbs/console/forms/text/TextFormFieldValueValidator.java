package wbs.console.forms.text;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("textFormFieldValueValidator")
@Accessors (fluent = true)
public
class TextFormFieldValueValidator
	implements FormFieldValueValidator <String> {

	// properties

	@Getter @Setter
	Long minimumLength;

	@Getter @Setter
	Long maximumLength;

	@Getter @Setter
	Boolean trim;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <String> genericValueOptional) {

		if (
			optionalIsNotPresent (
				genericValueOptional)
		) {
			return optionalAbsent ();
		}

		String genericValue =
			optionalGetRequired (
				genericValueOptional);

		if (trim) {

			genericValue =
				stringTrim (
					genericValue);

		}

		if (

			isNotNull (
				minimumLength)

			&& lessThan (
				genericValue.length (),
				minimumLength)

		) {

			return optionalOfFormat (
				"Must be at least %s characters",
				integerToDecimalString (
					minimumLength));

		}

		if (

			isNotNull (
				maximumLength)

			&& moreThan (
				genericValue.length (),
				maximumLength)

		) {

			return optionalOfFormat (
				"Must be no more than %s characters",
				integerToDecimalString (
					minimumLength));

		}

		return optionalAbsent ();

	}

}
