package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.NoSuchElementException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModule;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleFormManager {

	<Type>
	Optional <ConsoleFormType <Type>> getFormType (
			TaskLogger parentTaskLogger,
			String consoleModuleName,
			String name,
			Class <Type> containerClass);

	default <Type>
	ConsoleFormType <Type> getFormTypeRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String consoleModuleName,
			@NonNull String name,
			@NonNull Class <Type> containerClass) {

		return optionalOrThrow (
			getFormType (
				parentTaskLogger,
				consoleModuleName,
				name,
				containerClass),
			() -> new NoSuchElementException (
				stringFormat (
					"Form type %s ",
					name,
					"in console module %s ",
					consoleModuleName,
					"not found")));

	}

	<Type>
	ConsoleFormType <Type> createFormType (
			TaskLogger parentTaskLogger,
			ConsoleModule consoleModule,
			String formName,
			Class <Type> containerClass,
			FormType formType,
			Optional <String> columnFieldsNameOptional,
			Optional <String> rowFieldsNameOptional);

	<Type>
	ConsoleFormType <Type> createFormType (
			TaskLogger parentTaskLogger,
			String formName,
			Class <Type> containerClass,
			FormType formType,
			Optional <FormFieldSet <Type>> columnFieldsNameOptional,
			Optional <FormFieldSet <Type>> rowFieldsNameOptional);

}
