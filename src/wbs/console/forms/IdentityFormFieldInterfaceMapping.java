package wbs.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("identityFormFieldInterfaceMapping")
public
class IdentityFormFieldInterfaceMapping<Container,Type>
	implements FormFieldInterfaceMapping<Container,Type,Type> {

	@Override
	public
	Type interfaceToGeneric (
			Container container,
			Type value,
			List<String> errors) {

		return value;

	}

	@Override
	public
	Type genericToInterface (
			Container container,
			Type value) {

		return value;

	}

}
