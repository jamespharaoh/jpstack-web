package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

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
	void validate (
			@NonNull Optional<Long> genericValue,
			@NonNull List<String> errors) {

		if (! genericValue.isPresent ()) {
			return;
		}

		if (
			genericValue.get () < minimum
			|| genericValue.get () > maximum
		) {

			errors.add (
				stringFormat (
					"Value for \"%s\" must be between %s and %s",
					label (),
					minimum,
					maximum));

		}

	}

}
