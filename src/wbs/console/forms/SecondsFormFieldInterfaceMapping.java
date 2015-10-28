package wbs.console.forms;

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

		if (! interfaceValue.isPresent ()) {
			return Optional.<Integer>absent ();
		}

		if (interfaceValue.get ().isEmpty ()) {
			return Optional.<Integer>absent ();
		}

		Integer genericValue =
			intervalFormatter.processIntervalStringSeconds (
				interfaceValue.get ());

		if (genericValue == null) {

			errors.add (
				stringFormat (
					"Please enter a valid interval for '%s'",
					label));

			return null;

		}

		return Optional.of (
			genericValue);

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Integer> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<String>absent ();
		}

		return Optional.of (
			intervalFormatter.createIntervalStringSeconds (
				genericValue.get ()));

	}

}
