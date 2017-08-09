package wbs.console.forms.object;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.CodeUtils.isNotValidCode;
import static wbs.utils.string.CodeUtils.simplifyToCodeRelaxed;
import static wbs.utils.string.StringUtils.stringDoesNotMatch;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("nameFormFieldValueValidator")
public
class NameFormFieldValueValidator
	implements FormFieldValueValidator<String> {

	// dependencies

	@Getter @Setter
	Pattern codePattern;

	@Getter @Setter
	Pattern namePattern;

	@Getter @Setter
	String nameError;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <String> genericValue) {

		// validate name

		if (

			isNotNull (
				namePattern)

			&& stringDoesNotMatch (
				namePattern,
				genericValue.get ())

		) {

			return optionalOf (
				nameError);

		}

		// validate code

		String code =
			simplifyToCodeRelaxed (
				genericValue.get ());

		if (
			isNotNull (
				codePattern)
		) {

			if (
				stringDoesNotMatch (
					codePattern,
					code)
			) {

				return optionalOf (
					nameError);

			}

		} else {

			if (
				isNotValidCode (
					code)
			) {

				return optionalOf (
					ifNull (
						nameError,
						stringFormat (
							"Names must contain at least one letter, and ",
							"cannot have a digit before the first letter")));

			}

		}

		return optionalAbsent ();

	}

}
