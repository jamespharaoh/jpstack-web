package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("codeFormFieldValueValidator")
public
class CodeFormFieldValueValidator
	implements FormFieldValueValidator <String> {

	// dependencies

	@Getter @Setter
	Pattern pattern;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <String> genericValue) {

		Matcher matcher =
			pattern.matcher (
				genericValue.get ());

		if (! matcher.matches ()) {

			return optionalOfFormat (
				stringFormat (
					"Invalid code"));

		}

		return optionalAbsent ();

	}

}
