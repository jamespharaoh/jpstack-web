package wbs.console.forms;

import java.util.List;

import com.google.common.base.Optional;

public
interface FormFieldInterfaceMapping<Container,Generic,Interface> {

	Optional<Generic> interfaceToGeneric (
			Container container,
			Optional<Interface> interfaceValue,
			List<String> errors);

	Optional<Interface> genericToInterface (
			Container container,
			Optional<Generic> genericValue);

}
