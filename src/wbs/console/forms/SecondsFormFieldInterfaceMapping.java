package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
	Optional<Integer> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))

		) {

			return Optional.<Integer>absent ();

		} else {

			Integer genericValue =
				intervalFormatter.processIntervalStringSeconds (
					interfaceValue.get ());

			if (genericValue == null) {

				errors.add (
					stringFormat (
						"Please enter a valid interval for '%s'",
						label));

				return Optional.<Integer>absent ();

			}

			return Optional.<Integer>of (
				genericValue);

		}

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Integer> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.<String>absent ();

		} else {

			return Optional.of (
				intervalFormatter.createIntervalStringSeconds (
					genericValue.get ()));

		}

	}

}
