package wbs.console.forms;

import com.google.common.base.Optional;

public
class AbstractFormFieldPluginProvider
	implements FormFieldPluginProvider {

	@Override
	public
	Optional<FormFieldNativeMapping<?,?>> getNativeMapping (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass) {

		return Optional.absent ();

	}

	@Override
	public
	Optional<FormFieldUpdateHook<?,?,?>> getUpdateHook (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName) {

		return Optional.absent ();

	}

}
