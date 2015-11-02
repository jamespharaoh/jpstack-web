package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("yesNoCsvFormFieldInterfaceMapping")
public
class YesNoCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Boolean,String> {

	@Override
	public
	Optional<Boolean> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (
			equal (
				optionalRequired (
					interfaceValue),
				"yes")
		) {

			return Optional.<Boolean>of (
				true);

		} else if (
			equal (
				optionalRequired (
					interfaceValue),
				"no")
		) {

			return Optional.<Boolean>of (
				false);

		} else if (
			equal (
				optionalRequired (
					interfaceValue),
				"")
		) {

			return Optional.<Boolean>absent ();

		} else {

			throw new IllegalArgumentException ();

		}

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Boolean> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.<String>of (
				"");

		} else if (
			equal (
				optionalRequired (
					genericValue),
				true)
		) {

			return Optional.<String>of (
				"yes");

		} else if (
			equal (
				optionalRequired (
					genericValue),
				false)
		) {

			return Optional.<String>of (
				"no");

		} else {

			throw new IllegalArgumentException ();

		}

	}

}
