package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.codify;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
	Optional<String> read (
			@NonNull Container container) {

		return Optional.fromNullable (
			(String)
			BeanLogic.getProperty (
				container,
				consoleHelper.nameFieldName ()));

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue) {

		BeanLogic.setProperty (
			container,
			consoleHelper.nameFieldName (),
			nativeValue.get ());

		if (consoleHelper.codeExists ()) {

			String codeValue =
				codify (
					nativeValue.get ());

			BeanLogic.setProperty (
				container,
				consoleHelper.codeFieldName (),
				codeValue);

		}

	}

}
