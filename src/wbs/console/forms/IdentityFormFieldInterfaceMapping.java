package wbs.console.forms;

import static wbs.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

import fj.data.Either;

@PrototypeComponent ("identityFormFieldInterfaceMapping")
public
class IdentityFormFieldInterfaceMapping<Container,Type>
	implements FormFieldInterfaceMapping<Container,Type,Type> {

	@Override
	public
	Either<Optional<Type>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Type> value) {

		return successResult (
			value);

	}

	@Override
	public
	Either<Optional<Type>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Type> value) {

		return successResult (
			value);

	}

}
