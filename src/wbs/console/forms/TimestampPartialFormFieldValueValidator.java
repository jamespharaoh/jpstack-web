package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.validPartialTimestamp;

import java.util.List;

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
	void validate (
			@NonNull Optional<String> genericValue,
			@NonNull List<String> errors) {

		// allow null

		if (! genericValue.isPresent ()) {
			return;
		}

		// check it's a valid partial timestamp

		if (
			validPartialTimestamp (
				genericValue.get ())
		) {

			errors.add (
				"Invalid partial timestamp");

		}

	}

}
