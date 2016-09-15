package wbs.console.forms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.utils.etc.PropertyUtils;

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
			PropertyUtils.getProperty (
				container,
				consoleHelper.nameFieldName ()));

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue) {

		PropertyUtils.setProperty (
			container,
			consoleHelper.nameFieldName (),
			nativeValue.get ());

		if (consoleHelper.codeExists ()) {

			String codeValue =
				simplifyToCodeRequired (
					nativeValue.get ());

			PropertyUtils.setProperty (
				container,
				consoleHelper.codeFieldName (),
				codeValue);

		}

	}

}
