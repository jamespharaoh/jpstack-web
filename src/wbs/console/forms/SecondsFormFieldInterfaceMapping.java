package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.utils.IntervalFormatter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("secondsFormFieldInterfaceMapping")
public
class SecondsFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Long, String> {

	// singleton dependencies

	@SingletonDependency
	IntervalFormatter intervalFormatter;

	// properties

	@Getter @Setter
	String label;

	@Getter @Setter
	SecondsFormFieldSpec.Format format;

	// implementation

	@Override
	public
	Either <Optional <Long>, String> interfaceToGeneric (
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

			return successResult (
				Optional.<Long>absent ());

		} else {

			Optional <Long> genericValue =
				intervalFormatter.parseIntervalStringSeconds (
					interfaceValue.get ());

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return errorResult (
					stringFormat (
						"Please enter a valid interval for '%s'",
						label));

			}

			return successResult (
				Optional.of (
					genericValue.get ()));

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Long> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.<String>absent ());

		} else {

			switch (format) {

			case textual:

				return successResult (
					Optional.of (
						intervalFormatter.createTextualIntervalStringSeconds (
							genericValue.get ())));

			case numeric:

				return successResult (
					Optional.of (
						intervalFormatter.createNumericIntervalStringSeconds (
							genericValue.get ())));

			default:

				throw new RuntimeException ();

			}

		}

	}

}
