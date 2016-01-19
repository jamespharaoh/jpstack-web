package wbs.console.forms;

import com.google.common.base.Optional;

public
interface FormFieldPluginManager {

	Optional<FormFieldNativeMapping<?,?,?>> getNativeMapping (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass);

	FormFieldNativeMapping<?,?,?> getNativeMappingRequired (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass);

	FormFieldUpdateHook<?,?,?> getUpdateHook (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName);

}
