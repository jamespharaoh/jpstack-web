package wbs.console.forms;

import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

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

			isNotPresent (
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
