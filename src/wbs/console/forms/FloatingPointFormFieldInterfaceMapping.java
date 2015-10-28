package wbs.console.forms;

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

		if (! interfaceValue.isPresent ()) {
			return null;
		}

		if (interfaceValue.get ().isEmpty ()) {
			return null;
		}

		return Optional.of (
			Double.parseDouble (
				interfaceValue.get ()));

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Double> genericValue) {

		if (! genericValue.isPresent ()) {
			return null;
		}

		return Optional.of (
			Double.toString (
				genericValue.get ()));

	}

}
