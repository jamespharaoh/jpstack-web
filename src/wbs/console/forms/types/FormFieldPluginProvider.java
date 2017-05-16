package wbs.console.forms.types;

import com.google.common.base.Optional;

import wbs.console.forms.core.ConsoleFormBuilderContext;

public
interface FormFieldPluginProvider {

	default
	Optional<ConsoleFormNativeMapping<?,?,?>> getNativeMapping (
			ConsoleFormBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass) {

		return Optional.absent ();

	}

	default
	Optional<FormFieldUpdateHook<?,?,?>> getUpdateHook (
			ConsoleFormBuilderContext context,
			Class<?> containerClass,
			String fieldName) {

		return Optional.absent ();

	}

}
