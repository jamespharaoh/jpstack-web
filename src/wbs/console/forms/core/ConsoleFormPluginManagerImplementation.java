package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.forms.basic.NullFormFieldUpdateHook;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldPluginProvider;
import wbs.console.forms.types.FormFieldUpdateHook;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("consoleFormPluginManager")
public
class ConsoleFormPluginManagerImplementation
	implements ConsoleFormPluginManager {

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
	Optional <ConsoleFormNativeMapping <?,?,?>> getNativeMapping (
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass) {

		for (
			FormFieldPluginProvider pluginProvider
				: formFieldPluginProviders
		) {

			Optional<ConsoleFormNativeMapping<?,?,?>> optionalNativeMapping =
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

		return Optional.<ConsoleFormNativeMapping<?,?,?>>absent ();

	}

	@Override
	public
	ConsoleFormNativeMapping<?,?,?> getNativeMappingRequired (
			ConsoleFormBuilderContext context,
			Class<?> containerClass,
			String fieldName,
			Class<?> genericClass,
			Class<?> nativeClass) {

		Optional<ConsoleFormNativeMapping<?,?,?>> result =
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
			ConsoleFormBuilderContext context,
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
