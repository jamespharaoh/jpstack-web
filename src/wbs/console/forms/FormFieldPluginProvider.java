package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldPluginProvider {

	Optional<FormFieldNativeMapping<?,?>> getNativeMapping (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass);

	Optional<FormFieldUpdateHook<?,?,?>> getUpdateHook (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName);

}
