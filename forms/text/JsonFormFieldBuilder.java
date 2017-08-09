package wbs.console.forms.text;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import lombok.NonNull;

import wbs.console.forms.basic.ChainedFormFieldNativeMapping;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("jsonFormFieldBuilder")
public
class JsonFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ChainedFormFieldNativeMapping>
		chainedFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <HtmlFormFieldRenderer> htmlFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <JsonFormFieldNativeMapping>
		jsonFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <JsonFormFieldInterfaceMapping>
		jsonFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <Utf8StringFormFieldNativeMapping>
		utf8StringFormFieldNativeMappingProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	JsonFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation formFieldSet;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String name =
				spec.name ();

			String label =
				ifNull (
					spec.label (),
					capitalise (
						camelToSpaces (
							name)));

			// field type

			Class<?> propertyClass =
				PropertyUtils.propertyClassForClass (
					context.containerClass (),
					name);

			// accessor

			FormFieldAccessor accessor =
				simpleFormFieldAccessorProvider.provide (
					taskLogger)

				.name (
					name)

				.nativeClass (
					propertyClass);

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				ifThenElse (
					classEqualSafe (
						propertyClass,
						byte[].class),

				() ->
					chainedFormFieldNativeMappingProvider.provide (
						taskLogger)

					.previousMapping (
						jsonFormFieldNativeMappingProvider.provide (
							taskLogger))

					.nextMapping (
						utf8StringFormFieldNativeMappingProvider.provide (
							taskLogger)),

				() ->
					chainedFormFieldNativeMappingProvider.provide (
						taskLogger)

					.previousMapping (
						jsonFormFieldNativeMappingProvider.provide (
							taskLogger))

					.nextMapping (
						formFieldPluginManager.getNativeMappingRequired (
							taskLogger,
							context,
							context.containerClass (),
							name,
							String.class,
							propertyClass))

			);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				jsonFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// renderer

			FormFieldRenderer renderer =
				htmlFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label);

			// form field

			formFieldSet.addFormItem (
				readOnlyFormFieldProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

			);

		}

	}

}
