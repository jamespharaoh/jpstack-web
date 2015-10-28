package wbs.console.forms;

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

	@Override
	public
	Optional<Long> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (! interfaceValue.isPresent ()) {
			return Optional.<Long>absent ();
		}

		if (interfaceValue.get ().isEmpty ()) {
			return Optional.<Long>absent ();
		}

		return Optional.of (
			Long.parseLong (
				interfaceValue.get ()));

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Long> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<String>absent ();
		}

		if (genericValue.get () == 0 && blankIfZero) {
			return Optional.of ("");
		}

		return Optional.of (
			Long.toString (
				genericValue.get ()));

	}

}
