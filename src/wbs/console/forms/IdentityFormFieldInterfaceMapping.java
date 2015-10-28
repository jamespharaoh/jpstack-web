package wbs.console.forms;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("identityFormFieldInterfaceMapping")
public
class IdentityFormFieldInterfaceMapping<Container,Type>
	implements FormFieldInterfaceMapping<Container,Type,Type> {

	@Override
	public
	Optional<Type> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<Type> value,
			@NonNull List<String> errors) {

		return value;

	}

	@Override
	public
	Optional<Type> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Type> value) {

		return value;

	}

}
