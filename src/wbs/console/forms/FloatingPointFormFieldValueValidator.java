package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("doubleFormFieldValueValidator")
public
class FloatingPointFormFieldValueValidator
	implements FormFieldValueValidator<Double> {

	@Getter @Setter
	String label;

	@Getter @Setter
	Double minimum;

	@Getter @Setter
	Double maximum;

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<Double> genericValue) {

		if (! genericValue.isPresent ()) {

			return Optional.<String>absent ();

		}

		if (
			genericValue.get () < minimum
			|| genericValue.get () > maximum
		) {

			return Optional.of (
				stringFormat (
					"Value for \"%s\" must be between %s and %s",
					label (),
					minimum,
					maximum));

		}

		return Optional.<String>absent ();

	}

}
