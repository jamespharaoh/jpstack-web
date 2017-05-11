package wbs.console.forms.context;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface FormContextManager {

	<Type>
	Optional <FormContextBuilder <Type>> formContextBuilder (
			String consoleModuleName,
			String name,
			Class <Type> containerClass);

	default <Type>
	FormContextBuilder <Type> formContextBuilderRequired (
			@NonNull String consoleModuleName,
			@NonNull String name,
			@NonNull Class <Type> containerClass) {

		return optionalGetRequired (
			formContextBuilder (
				consoleModuleName,
				name,
				containerClass));

	}

}
