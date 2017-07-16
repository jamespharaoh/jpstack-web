package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.basic.NullFormFieldUpdateHook;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldPluginProvider;
import wbs.console.forms.types.FormFieldUpdateHook;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleFormPluginManager")
public
class ConsoleFormPluginManagerImplementation
	implements ConsoleFormPluginManager {

	// singleton dependencies

	@SingletonDependency
	List <FormFieldPluginProvider> formFieldPluginProviders;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <NullFormFieldUpdateHook <?,?,?>>
		nullFormFieldUpdateHookProvider;

	// implementation

	@Override
	public
	Optional <ConsoleFormNativeMapping <?,?,?>> getNativeMapping (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormBuilderContext context,
			@NonNull Class <?> containerClass,
			@NonNull String fieldName,
			@NonNull Class <?> genericClass,
			@NonNull Class <?> nativeClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getNativeMapping");

		) {

			for (
				FormFieldPluginProvider pluginProvider
					: formFieldPluginProviders
			) {

				Optional<ConsoleFormNativeMapping<?,?,?>> optionalNativeMapping =
					pluginProvider.getNativeMapping (
						taskLogger,
						context,
						containerClass,
						fieldName,
						genericClass,
						nativeClass);

				if (optionalNativeMapping.isPresent ()) {
					return optionalNativeMapping;
				}

			}

			return optionalAbsent ();

		}

	}

	@Override
	public
	ConsoleFormNativeMapping <?, ?, ?> getNativeMappingRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormBuilderContext context,
			@NonNull Class <?> containerClass,
			@NonNull String fieldName,
			@NonNull Class <?> genericClass,
			@NonNull Class <?> nativeClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getNativeMappingRequired");

		) {

			Optional <ConsoleFormNativeMapping <?, ?, ?>> result =
				getNativeMapping (
					taskLogger,
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

	}

	@Override
	public
	FormFieldUpdateHook <?, ?, ?> getUpdateHook (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormBuilderContext context,
			@NonNull Class <?> containerClass,
			@NonNull String fieldName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getUpdateHook");

		) {

			for (
				FormFieldPluginProvider pluginProvider
					: formFieldPluginProviders
			) {

				Optional<FormFieldUpdateHook<?,?,?>> optionalUpdateHook =
					pluginProvider.getUpdateHook (
						taskLogger,
						context,
						containerClass,
						fieldName);

				if (optionalUpdateHook.isPresent ()) {
					return optionalUpdateHook.get ();
				}

			}

			return nullFormFieldUpdateHookProvider.provide (
				taskLogger);

		}

	}

}
