package wbs.console.forms;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("htmlFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class HtmlFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <HtmlFormFieldRenderer>
	htmlFormFieldRendererProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	HtmlFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

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

		/*
			.dynamic (
				false)
		*/

			.nativeClass (
				propertyClass);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			formFieldPluginManager.getNativeMappingRequired (
				context,
				context.containerClass (),
				name,
				String.class,
				propertyClass);

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

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
