package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
	Optional<Long> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))

		) {

			return Optional.<Long>absent ();

		} else {

			return Optional.of (
				Long.parseLong (
					interfaceValue.get ()));

		}

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Long> genericValue) {

		if (

			isNotPresent (
				genericValue)

			|| (

				blankIfZero

				&& equal (
					genericValue.get (),
					0))

		) {

			return Optional.<String>absent ();

		} else {

			return Optional.of (
				Long.toString (
					genericValue.get ()));

		}

	}

}
