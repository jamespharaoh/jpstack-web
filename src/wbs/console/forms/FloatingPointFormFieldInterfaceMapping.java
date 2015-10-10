package wbs.console.forms;

import java.util.List;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("doubleFormFieldInterfaceMapping")
public
class FloatingPointFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Double,String> {

	@Override
	public
	Double interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		return Double.parseDouble (
			interfaceValue);

	}

	@Override
	public
	String genericToInterface (
			Container container,
			Double genericValue) {

		if (genericValue == null)
			return null;

		return Double.toString (
			genericValue);

	}

}
