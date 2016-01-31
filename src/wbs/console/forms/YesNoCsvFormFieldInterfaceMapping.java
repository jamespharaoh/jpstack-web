package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

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
	Either<Optional<Boolean>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		if (
			equal (
				optionalRequired (
					interfaceValue),
				"yes")
		) {

			return successResult (
				Optional.<Boolean>of (
					true));

		} else if (
			equal (
				optionalRequired (
					interfaceValue),
				"no")
		) {

			return successResult (
				Optional.<Boolean>of (
					false));

		} else if (
			equal (
				optionalRequired (
					interfaceValue),
				"")
		) {

			if (nullable ()) {

				return successResult (
					Optional.<Boolean>absent ());

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
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Boolean> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.<String>of (
					""));

		} else if (
			equal (
				optionalRequired (
					genericValue),
				true)
		) {

			return successResult (
				Optional.<String>of (
					"yes"));

		} else if (
			equal (
				optionalRequired (
					genericValue),
				false)
		) {

			return successResult (
				Optional.<String>of (
					"no"));

		} else {

			throw new IllegalArgumentException ();

		}

	}

}
