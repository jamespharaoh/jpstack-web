package wbs.console.forms;

import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

import fj.data.Either;

@PrototypeComponent ("doubleFormFieldInterfaceMapping")
public
class FloatingPointFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Double,String> {

	@Override
	public
	Either<Optional<Double>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
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
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Double> genericValue) {

		if (
			optionalIsNotPresent (
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
