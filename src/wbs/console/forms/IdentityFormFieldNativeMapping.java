package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;

@PrototypeComponent ("identityFormFieldNativeMapping")
public
class IdentityFormFieldNativeMapping <Container, Type>
	implements FormFieldNativeMapping <Container, Type, Type> {

	@Override
	public
	Optional <Type> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Type> nativeValue) {

		return nativeValue;

	}

	@Override
	public
	Optional <Type> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Type> genericValue) {

		return genericValue;

	}

}
