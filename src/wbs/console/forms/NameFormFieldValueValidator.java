package wbs.console.forms;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCode;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

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
			isNotPresent (
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
