package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("jsonFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class JsonFormFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChainedFormFieldNativeMapping>
	chainedFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <HtmlFormFieldRenderer>
	htmlFormFieldRendererProvider;

	@PrototypeDependency
	Provider <JsonFormFieldNativeMapping>
	jsonFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <JsonFormFieldInterfaceMapping>
	jsonFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <Utf8StringFormFieldNativeMapping>
	utf8StringFormFieldNativeMappingProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	JsonFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

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
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

			// native mapping

			FormFieldNativeMapping nativeMapping =
				ifThenElse (
					classEqualSafe (
						propertyClass,
						byte[].class),

				() ->
					chainedFormFieldNativeMappingProvider.get ()

					.previousMapping (
						jsonFormFieldNativeMappingProvider.get ())

					.nextMapping (
						utf8StringFormFieldNativeMappingProvider.get ()),

				() ->
					chainedFormFieldNativeMappingProvider.get ()

					.previousMapping (
						jsonFormFieldNativeMappingProvider.get ())

					.nextMapping (
						formFieldPluginManager.getNativeMappingRequired (
							context,
							context.containerClass (),
							name,
							String.class,
							propertyClass))

			);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				jsonFormFieldInterfaceMappingProvider.get ();

			// renderer

			FormFieldRenderer renderer =
				htmlFormFieldRendererProvider.get ()

				.name (
					name)

				.label (
					label);

			// form field

			formFieldSet.addFormItem (
				readOnlyFormFieldProvider.get ()

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
