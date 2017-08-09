package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTimeZone;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("timezoneFormFieldValueValidator")
public
class TimezoneFormFieldValueValidator
	implements FormFieldValueValidator <String> {

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

		try {

			DateTimeZone.forID (
				genericValue);

		} catch (IllegalArgumentException exception) {

			return optionalOf (
				stringFormat (
					"Timezone not recognised: %s",
					genericValue));

		}

		return optionalAbsent ();

	}

}
