package wbs.console.forms;

import java.util.List;

public
interface FormFieldInterfaceMapping<Container,Generic,Interface> {

	Generic interfaceToGeneric (
			Container container,
			Interface interfaceValue,
			List<String> errors);

	Interface genericToInterface (
			Container container,
			Generic genericValue);

}
