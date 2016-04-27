package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.IntervalFormatter;

@Accessors (fluent = true)
@PrototypeComponent ("secondsFormFieldInterfaceMapping")
public
class SecondsFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Long,String> {

	// dependencies

	@Inject
	IntervalFormatter intervalFormatter;

	// properties

	@Getter @Setter
	String label;

	@Getter @Setter
	SecondsFormFieldSpec.Format format;

	// implementation

	@Override
	public
	Either<Optional<Long>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.<Long>absent ());

		} else {

			Optional<Integer> genericValue =
				intervalFormatter.parseIntervalStringSeconds (
					interfaceValue.get ());

			if (
				isNotPresent (
					genericValue)
			) {

				return errorResult (
					stringFormat (
						"Please enter a valid interval for '%s'",
						label));

			}

			return successResult (
				Optional.of (
					(long)
					genericValue.get ()));

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Long> genericValue) {

		if (
			isNotPresent (
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
							(int) (long)
							genericValue.get ())));

			case numeric:

				return successResult (
					Optional.of (
						intervalFormatter.createNumericIntervalStringSeconds (
							(int) (long)
							genericValue.get ())));

			default:

				throw new RuntimeException ();

			}

		}

	}

}
