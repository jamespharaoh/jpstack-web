package wbs.platform.event.console;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.FormFieldAccessor;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.forms.FormFieldPluginManagerImplementation;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.HtmlFormFieldRenderer;
import wbs.console.forms.IdentityFormFieldAccessor;
import wbs.console.forms.IdentityFormFieldNativeMapping;
import wbs.console.forms.ReadOnlyFormField;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("eventDetailsFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class EventDetailsFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <EventDetailsFormFieldInterfaceMapping>
	eventDetailsFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <HtmlFormFieldRenderer>
	htmlFormFieldRendererProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldAccessor>
	identityFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	EventDetailsFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			"details";

		String label =
			"Details";

		// accessor

		FormFieldAccessor accessor =
			identityFormFieldAccessorProvider.get ()

			.containerClass (
				context.containerClass ());

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			eventDetailsFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			htmlFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label);

		// form field

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

			.renderer (
				renderer)

		);

	}

}
