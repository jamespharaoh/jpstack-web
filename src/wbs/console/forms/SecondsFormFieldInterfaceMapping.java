package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.misc.IntervalFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("secondsFormFieldInterfaceMapping")
public
class SecondsFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Integer,String> {

	// dependencies

	@Inject
	IntervalFormatter intervalFormatter;

	// properties

	@Getter @Setter
	String label;

	// implementation

	@Override
	public
	Either<Optional<Integer>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.<Integer>absent ());

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
				genericValue);

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Integer> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.<String>absent ());

		} else {

			return successResult (
				Optional.of (
					intervalFormatter.createIntervalStringSeconds (
						genericValue.get ())));

		}

	}

}
