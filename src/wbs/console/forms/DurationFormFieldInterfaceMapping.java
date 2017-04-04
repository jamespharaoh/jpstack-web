package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResultAbsent;
import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.time.DurationFormatter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("durationFormFieldInterfaceMapping")
public
class DurationFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Duration, String> {

	// singleton dependencies

	@SingletonDependency
	DurationFormatter durationFormatter;

	// properties

	@Getter @Setter
	String label;

	@Getter @Setter
	Format format;

	// implementation

	@Override
	public
	Either <Optional <Duration>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				optionalGetRequired (
					interfaceValue))

		) {

			return successResultAbsent ();

		} else {

			Optional <Duration> genericValue =
				durationFormatter.stringToDuration (
					optionalGetRequired (
						interfaceValue));

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return errorResultFormat (
					"Please enter a valid interval for '%s'",
					label);

			}

			return successResultPresent (
				genericValue.get ());

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Duration> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResultAbsent ();

		} else {

			switch (format) {

			case textual:

				return successResultPresent (
					durationFormatter.durationToStringTextual (
						optionalGetRequired (
							genericValue)));

			case numeric:

				return successResultPresent (
					durationFormatter.durationToStringNumeric (
						optionalGetRequired (
							genericValue)));

			default:

				throw new RuntimeException ();

			}

		}

	}

	public static
	enum Format {
		textual,
		numeric;
	}

}
