package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.codify;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

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
	void validate (
			@NonNull Optional<String> genericValue,
			@NonNull List<String> errors) {

		String code =
			codify (
				genericValue.get ());

		Matcher matcher =
			codePattern.matcher (
				code);

		if (! matcher.matches ()) {

			errors.add (
				stringFormat (
					"Invalid name"));

		}

	}

}
