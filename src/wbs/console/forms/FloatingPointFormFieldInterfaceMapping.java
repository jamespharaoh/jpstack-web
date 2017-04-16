package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

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
				optionalAbsent ());

		} else {

			return successResult (
				optionalOf (
					Double.parseDouble (
						interfaceValue.get ())));

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Double> genericValue) {

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
