package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.helper.ConsoleHelper;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("nameFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class NameFormFieldBuilder {

	// prototype dependencies

	@Inject
	Provider<NameFormFieldUpdateHook>
	nameFormFieldUpdateHookProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<NameFormFieldConstraintValidator>
	nameFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<NameFormFieldValueValidator>
	nameFormFieldValueValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<NameFormFieldAccessor>
	nameFormFieldAccessorProvider;

	@Inject
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	NameFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		ConsoleHelper consoleHelper =
			context.consoleHelper ();

		String name =
			ifNull (
				spec.name (),
				consoleHelper.nameFieldName ());

		String label =
			ifNull (
				spec.label (),
				capitalise (consoleHelper.nameLabel ()));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		// accessor

		FormFieldAccessor accessor =
			nameFormFieldAccessorProvider.get ()

			.consoleHelper (
				consoleHelper);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// value validator

		FormFieldValueValidator valueValidator =
			nameFormFieldValueValidatorProvider.get ()

			.codePattern (
				CodeFormFieldValueValidator.defaultPattern);

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nameFormFieldValueConstraintValidatorProvider.get ();

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

			.nullable (
				false)

			.size (
				FormField.defaultSize);

		// update hook

		FormFieldUpdateHook updateHook =
			nameFormFieldUpdateHookProvider.get ();

		// field

		if (readOnly) {

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

		} else {

			formFieldSet.formFields ().add (

				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.valueValidator (
					valueValidator)

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
