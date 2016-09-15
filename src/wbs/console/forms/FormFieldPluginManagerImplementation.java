package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("formFieldPluginManager")
public
class FormFieldPluginManagerImplementation
	implements FormFieldPluginManager {

	// singleton dependencies

	@SingletonDependency
	List <FormFieldPluginProvider> formFieldPluginProviders;

	// prototype dependencies

	@PrototypeDependency
	Provider <NullFormFieldUpdateHook <?,?,?>>
	nullFormFieldUpdateHookProvider;

	// implementation

	@Override
	public
	Optional <FormFieldNativeMapping <?,?,?>> getNativeMapping (
			FormFieldBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass) {

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
			optionalIsPresent (
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
