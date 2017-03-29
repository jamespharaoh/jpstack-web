package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.string.CodeUtils.simplifyToCodeRelaxed;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("nameFormFieldAccessor")
public
class NameFormFieldAccessor <Container>
	implements FormFieldAccessor <Container, String> {

	// properties

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	// implementation

	@Override
	public
	Optional <String> read (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container) {

		return optionalFromNullable (
			(String)
			PropertyUtils.propertyGetAuto (
				container,
				consoleHelper.nameFieldName ()));

	}

	@Override
	public
	void write (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		PropertyUtils.propertySetAuto (
			container,
			consoleHelper.nameFieldName (),
			nativeValue.get ());

		if (consoleHelper.codeExists ()) {

			String codeValue =
				simplifyToCodeRelaxed (
					nativeValue.get ());

			PropertyUtils.propertySetAuto (
				container,
				consoleHelper.codeFieldName (),
				codeValue);

		}

	}

}
