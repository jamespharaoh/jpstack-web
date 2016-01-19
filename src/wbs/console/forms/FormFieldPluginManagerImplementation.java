package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("formFieldPluginManager")
public
class FormFieldPluginManagerImplementation
	implements FormFieldPluginManager {

	// prototype dependencies

	@Inject
	Provider<NullFormFieldUpdateHook<?,?,?>>
	nullFormFieldUpdateHookProvider;

	// collection dependencies

	@Inject
	List<FormFieldPluginProvider> formFieldPluginProviders;

	// implementation

	@Override
	public
	Optional<FormFieldNativeMapping<?,?,?>> getNativeMapping (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass) {

		for (
			FormFieldPluginProvider pluginProvider
				: formFieldPluginProviders
		) {

			Optional<FormFieldNativeMapping<?,?,?>> optionalNativeMapping =
				pluginProvider.getNativeMapping (
					context,
					containerClass,
					fieldName,
					genericClass,
					nativeClass);

			if (optionalNativeMapping.isPresent ()) {
				return optionalNativeMapping;
			}

		}

		return Optional.<FormFieldNativeMapping<?,?,?>>absent ();

	}

	@Override
	public
	FormFieldNativeMapping<?,?,?> getNativeMappingRequired (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass) {

		Optional<FormFieldNativeMapping<?,?,?>> result =
			getNativeMapping (
				context,
				containerClass,
				fieldName,
				genericClass,
				nativeClass);

		if (
			isPresent (
				result)
		) {

			return result.get ();

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to natively map %s as %s for %s.%s",
					genericClass.getSimpleName (),
					nativeClass.getSimpleName (),
					containerClass.getSimpleName (),
					fieldName));

		}

	}

	@Override
	public
	FormFieldUpdateHook<?,?,?> getUpdateHook (
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName) {

		for (
			FormFieldPluginProvider pluginProvider
				: formFieldPluginProviders
		) {

			Optional<FormFieldUpdateHook<?,?,?>> optionalUpdateHook =
				pluginProvider.getUpdateHook (
					context,
					containerClass,
					fieldName);

			if (optionalUpdateHook.isPresent ()) {
				return optionalUpdateHook.get ();
			}

		}

		return nullFormFieldUpdateHookProvider.get ();

	}

}
