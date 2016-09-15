package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.CodeUtils.simplifyToCode;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("nameFormFieldValueValidator")
public
class NameFormFieldValueValidator
	implements FormFieldValueValidator<String> {

	// dependencies

	@Getter @Setter
	Pattern codePattern;

	// implementation

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<String> genericValue) {

		Optional<String> code =
			simplifyToCode (
				genericValue.get ());

		if (
			optionalIsNotPresent (
				code)
		) {

			return Optional.of (
				stringFormat (
					"Names must contain at least one letter, and cannot have ",
					"a digit before the first letter"));


		}

		return Optional.<String>absent ();

	}

}
