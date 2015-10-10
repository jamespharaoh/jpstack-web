package wbs.console.forms;

public
interface FormFieldPluginManager {

	FormFieldNativeMapping<?,?> getNativeMapping (
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
