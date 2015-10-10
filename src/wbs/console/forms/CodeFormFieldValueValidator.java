package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
	void validate (
			String genericValue,
			List<String> errors) {

		Matcher matcher =
			pattern.matcher (
				genericValue);

		if (! matcher.matches ()) {

			errors.add (
				stringFormat (
					"Invalid code"));

		}

	}

	public static final
	Pattern defaultPattern =
		Pattern.compile (
			joinWithoutSeparator (
				"^",
				"([a-z][a-z0-9]*)",
				"(_([a-z0-9]+))*",
				"$"));

}
