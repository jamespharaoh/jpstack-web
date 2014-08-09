package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;

import com.google.common.collect.ImmutableList;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("delegateFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class DelegateFormFieldBuilder {

	// prototype dependencies

	@Inject
	Provider<ChainFormFieldAccessor>
	chainFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	DelegateFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			joinWithSeparator (
				"_",
				spec.delegate (),
				spec.name ());

		String label =
			spec.label ();

		// accessor

		FormFieldAccessor accessor =
			chainFormFieldAccessorProvider.get ()

			.accessors (
				ImmutableList.<FormFieldAccessor>of (

				simpleFormFieldAccessorProvider.get ()

					.name (
						spec.delegate ())

					.nativeClass (
						Object.class),

				simpleFormFieldAccessorProvider.get ()

					.name (
						spec.name ())

					.nativeClass (
						String.class))

			);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.size (
				FormField.defaultSize);

		// field

		formFieldSet.formFields ().add (

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
