package wbs.platform.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("integerFormFieldInterfaceMapping")
public
class IntegerFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Long,String> {

	@Override
	public
	Long interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		return Long.parseLong (
			interfaceValue);

	}

	@Override
	public
	String genericToInterface (
			Container container,
			Long genericValue) {

		if (genericValue == null)
			return null;

		return Long.toString (
			genericValue);

	}

}
