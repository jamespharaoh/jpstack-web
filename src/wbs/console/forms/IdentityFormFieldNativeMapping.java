package wbs.console.forms;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("identityFormFieldNativeMapping")
public
class IdentityFormFieldNativeMapping<Container,Type>
	implements FormFieldNativeMapping<Container,Type,Type> {

	@Override
	public
	Optional<Type> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<Type> nativeValue) {

		return nativeValue;

	}

	@Override
	public
	Optional<Type> genericToNative (
			@NonNull Container container,
			@NonNull Optional<Type> genericValue) {

		return genericValue;

	}

}
