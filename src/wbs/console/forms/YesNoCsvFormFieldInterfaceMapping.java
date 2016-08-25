package wbs.console.forms;

import static wbs.framework.utils.etc.LogicUtils.booleanEqual;
import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;

import java.util.Map;

import com.google.common.base.Optional;

import fj.data.Either;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("yesNoCsvFormFieldInterfaceMapping")
public
class YesNoCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Boolean,String> {

	// properties

	@Getter @Setter
	Boolean nullable;

	// implementation

	@Override
	public
	Either <Optional <Boolean>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceOptional) {

		String interfaceValue =
			optionalGetRequired (
				interfaceOptional);

		if (
			stringEqualSafe (
				interfaceValue,
				"yes")
		) {

			return successResult (
				Optional.of (
					true));

		} else if (
			stringEqualSafe (
				interfaceValue,
				"no")
		) {

			return successResult (
				Optional.of (
					false));

		} else if (
			stringEqualSafe (
				interfaceValue,
				"")
		) {

			if (nullable ()) {

				return successResult (
					Optional.absent ());

			} else {

				return errorResult (
					"This is a required field");

			}

		} else {

			if (nullable ()) {

				return errorResult (
					"This field must contain 'yes' or 'no', or be empty");

			} else {

				return errorResult (
					"This field must contain 'yes' or 'no'");

			}

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Boolean> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					""));

		} else if (
			booleanEqual (
				optionalGetRequired (
					genericValue),
				true)
		) {

			return successResult (
				Optional.of (
					"yes"));

		} else if (
			booleanEqual (
				optionalGetRequired (
					genericValue),
				false)
		) {

			return successResult (
				Optional.of (
					"no"));

		} else {

			throw new IllegalArgumentException ();

		}

	}

}
