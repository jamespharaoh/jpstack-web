package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.validInterval;
import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("timestampPartialFormFieldValueValidator")
public
class TimestampPartialFormFieldValueValidator
	implements FormFieldValueValidator<String> {

	// implementation

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<String> genericValue) {

		// allow null

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.<String>absent ();

		}

		// check it's a valid partial timestamp

		if (
			! validInterval (
				genericValue.get ())
		) {

			return Optional.of (
				"Invalid interval");

		}

		return Optional.<String>absent ();

	}

}
