package wbs.console.forms;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("identityFormFieldNativeMapping")
public
class IdentityFormFieldNativeMapping<Type>
	implements FormFieldNativeMapping<Type,Type> {

	@Override
	public
	Type nativeToGeneric (
			Type nativeValue) {

		return nativeValue;

	}

	@Override
	public
	Type genericToNative (
			Type genericValue) {

		return genericValue;

	}

}
