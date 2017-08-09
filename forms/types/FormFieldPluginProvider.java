package wbs.console.forms.types;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import wbs.console.forms.core.ConsoleFormBuilderContext;

import wbs.framework.logging.TaskLogger;

public
interface FormFieldPluginProvider {

	default
	Optional <ConsoleFormNativeMapping <?, ?, ?>> getNativeMapping (
			TaskLogger parentTaskLogger,
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass) {

		return optionalAbsent ();

	}

	default
	Optional <FormFieldUpdateHook <?, ?, ?>> getUpdateHook (
			TaskLogger parentTaskLogger,
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName) {

		return optionalAbsent ();

	}

}
