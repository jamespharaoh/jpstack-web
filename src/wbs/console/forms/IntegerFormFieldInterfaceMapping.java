package wbs.console.forms;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("integerFormFieldInterfaceMapping")
public
class IntegerFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Long,String> {

	// properties

	@Getter @Setter
	Boolean blankIfZero = false;

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

		if (genericValue == 0 && blankIfZero)
			return "";

		return Long.toString (
			genericValue);

	}

}
