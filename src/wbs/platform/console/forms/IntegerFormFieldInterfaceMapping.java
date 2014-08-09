package wbs.platform.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("integerFormFieldInterfaceMapping")
public
class IntegerFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Integer,String> {

	@Override
	public
	Integer interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		return Integer.parseInt (
			interfaceValue);

	}

	@Override
	public
	String genericToInterface (
			Container container,
			Integer genericValue) {

		if (genericValue == null)
			return null;

		return Integer.toString (
			genericValue);

	}

}
