package wbs.console.forms.basic;

import static wbs.utils.etc.LogicUtils.equalSafe;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.types.FormFieldPluginProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("identityFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class IdentityFormFieldPluginProvider
	implements FormFieldPluginProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldNativeMapping>
		identityFormFieldNativeMappingProvider;

	// implementation

	@Override
	public
	Optional getNativeMapping (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormBuilderContext context,
			@NonNull Class containerClass,
			@NonNull String fieldName,
			@NonNull Class genericClass,
			@NonNull Class nativeClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getNativeMapping");

		) {

			if (
				equalSafe (
					genericClass,
					nativeClass)
			) {

				return Optional.of (
					identityFormFieldNativeMappingProvider.provide (
						taskLogger));

			} else {

				return Optional.absent ();

			}

		}

	}

}
