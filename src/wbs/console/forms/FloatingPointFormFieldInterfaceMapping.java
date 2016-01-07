package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.successResult;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("doubleFormFieldInterfaceMapping")
public
class FloatingPointFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Double,String> {

	@Override
	public
	Either<Optional<Double>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				interfaceValue.get ())

		) {

			return successResult (
				Optional.<Double>absent ());

		} else {

			return successResult (
				Optional.of (
					Double.parseDouble (
						interfaceValue.get ())));

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Double> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.<String>of (
					""));

		} else {

			return successResult (
				Optional.of (
					Double.toString (
						genericValue.get ())));

		}

	}

}
