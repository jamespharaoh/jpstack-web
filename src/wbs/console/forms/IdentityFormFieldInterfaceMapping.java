package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.successResult;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("identityFormFieldInterfaceMapping")
public
class IdentityFormFieldInterfaceMapping<Container,Type>
	implements FormFieldInterfaceMapping<Container,Type,Type> {

	@Override
	public
	Either<Optional<Type>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<Type> value) {

		return successResult (
			value);

	}

	@Override
	public
	Either<Optional<Type>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Type> value) {

		return successResult (
			value);

	}

}
