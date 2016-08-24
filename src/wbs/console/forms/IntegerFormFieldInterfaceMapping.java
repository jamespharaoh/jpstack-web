package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.NumberUtils.equalToZero;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import fj.data.Either;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("integerFormFieldInterfaceMapping")
public
class IntegerFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Long,String> {

	// properties

	@Getter @Setter
	Boolean blankIfZero = false;

	// implementation

	@Override
	public
	Either<Optional<Long>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		// handle not present or empty

		if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				optionalGetRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.<Long>absent ());

		}

		// parse integer

		try {

			return successResult (
				Optional.of (
					Long.parseLong (
						interfaceValue.get ())));

		} catch (NumberFormatException exception) {

			return errorResult (
				stringFormat (
					"You must enter a whole number using digits"));

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Long> genericValue) {

		if (

			optionalIsNotPresent (
				genericValue)

			|| (

				blankIfZero

				&& equalToZero (
					genericValue.get ())

			)

		) {

			return successResult (
				Optional.absent ());

		} else {

			return successResult (
				Optional.of (
					Long.toString (
						genericValue.get ())));

		}

	}

}
