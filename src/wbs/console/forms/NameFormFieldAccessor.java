package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.codify;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("nameFormFieldAccessor")
public
class NameFormFieldAccessor<Container>
	implements FormFieldAccessor<Container,String> {

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	// implementation

	@Override
	public
	String read (
			Container container) {

		return
			(String)
			BeanLogic.getProperty (
				container,
				consoleHelper.nameFieldName ());

	}

	@Override
	public
	void write (
			Container container,
			String nativeValue) {

		BeanLogic.setProperty (
			container,
			consoleHelper.nameFieldName (),
			nativeValue);

		if (consoleHelper.codeExists ()) {

			String codeValue =
				codify (
					nativeValue);

			BeanLogic.setProperty (
				container,
				consoleHelper.codeFieldName (),
				codeValue);

		}

	}

}
