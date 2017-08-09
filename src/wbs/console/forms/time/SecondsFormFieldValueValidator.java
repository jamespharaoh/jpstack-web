package wbs.console.forms.time;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.time.duration.DurationFormatter;

@Accessors (fluent = true)
@PrototypeComponent ("secondsFormFieldInterfaceMapping")
public
class SecondsFormFieldValueValidator
	implements FormFieldValueValidator <Duration> {

	// singleton dependencies

	@SingletonDependency
	DurationFormatter durationFormatter;

	// properties

	@Getter @Setter
	String label;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <Duration> genericValueOptional) {

		if (
			optionalIsNotPresent (
				genericValueOptional)
		) {

			return optionalAbsent ();

		}

		Duration genericValue =
			optionalGetRequired (
				genericValueOptional);

		if (genericValue.getMillis () % 1000 != 0) {

			return optionalOfFormat (
				"Please enter a whole number of seconds for '%s'",
				label);

		}

		return optionalAbsent ();

	}

}
