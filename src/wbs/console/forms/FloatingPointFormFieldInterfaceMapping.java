package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("doubleFormFieldInterfaceMapping")
public
class FloatingPointFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Double,String> {

	@Override
	public
	Optional<Double> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				interfaceValue.get ())

		) {

			return Optional.<Double>absent ();

		} else {

			return Optional.of (
				Double.parseDouble (
					interfaceValue.get ()));

		}

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Double> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.<String>of ("");

		} else {

			return Optional.of (
				Double.toString (
					genericValue.get ()));

		}

	}

}
