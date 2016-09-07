package wbs.console.forms;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("integerFormFieldValueValidator")
public
class IntegerFormFieldValueValidator
	implements FormFieldValueValidator<Long> {

	@Getter @Setter
	String label;

	@Getter @Setter
	Long minimum;

	@Getter @Setter
	Long maximum;

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<Long> genericValue) {

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
