package wbs.console.forms;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import com.google.common.base.Optional;

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
