package wbs.console.forms;

import static wbs.framework.utils.etc.NullUtils.ifNull;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("idFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class IdFormFieldBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IntegerFormFieldInterfaceMapping>
	integerFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	IdFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			ifNull (
				spec.name (),
				"id");

		String label =
			ifNull (
				spec.label (),
				"Id");

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				Long.class);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			integerFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				false);

		// field

		formFieldSet.addFormField (
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

			.csvMapping (
				interfaceMapping)

			.renderer (
				renderer)

		);

	}

}
