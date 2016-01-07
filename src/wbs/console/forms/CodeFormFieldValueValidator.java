package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("codeFormFieldValueValidator")
public
class CodeFormFieldValueValidator
	implements FormFieldValueValidator<String> {

	// dependencies

	@Getter @Setter
	Pattern pattern;

	// implementation

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<String> genericValue) {

		Matcher matcher =
			pattern.matcher (
				genericValue.get ());

		if (! matcher.matches ()) {

			return Optional.of (
				stringFormat (
					"Invalid code"));

		}

		return Optional.<String>absent ();

	}

}
