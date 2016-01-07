package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.successResult;
import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("yesNoCsvFormFieldInterfaceMapping")
public
class YesNoCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Boolean,String> {

	@Override
	public
	Either<Optional<Boolean>,String> interfaceToGeneric (
			@NonNull Container container,
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

			return successResult (
				Optional.<Boolean>absent ());

		} else {

			throw new IllegalArgumentException ();

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
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
