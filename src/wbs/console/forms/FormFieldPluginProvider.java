package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldPluginProvider {

	default
	Optional<FormFieldNativeMapping<?,?,?>> getNativeMapping (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass) {

		return Optional.absent ();

	}

	default
	Optional<FormFieldUpdateHook<?,?,?>> getUpdateHook (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName) {

		return Optional.absent ();

	}

}
