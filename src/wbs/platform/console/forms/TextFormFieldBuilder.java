package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
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
import wbs.framework.utils.etc.BeanLogic;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.text.model.TextRec;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("textFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TextFormFieldBuilder {

	// prototype dependencies

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<NullFormFieldValueValidator>
	nullFormFieldValueValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider<SimpleFormFieldUpdateHook>
	simpleFormFieldUpdateHookProvider;

	@Inject
	Provider<TextFormFieldNativeMapping>
	textFormFieldNativeMappingProvider;

	@Inject
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext formFieldBuilderContext;

	@BuilderSource
	TextFormFieldSpec textFormFieldSpec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			textFormFieldSpec.name ();

		String label =
			ifNull (
				textFormFieldSpec.label (),
				capitalise (camelToSpaces (name)));

		Boolean readOnly =
			ifNull (
				textFormFieldSpec.readOnly (),
				false);

		Boolean nullable =
			ifNull (
				textFormFieldSpec.nullable (),
				false);

		Integer size =
			ifNull (
				textFormFieldSpec.size (),
				FormField.defaultSize);

		// field type

		Class<?> propertyClass =
			BeanLogic.propertyClass (
				formFieldBuilderContext.containerClass (),
				name);

		FormFieldAccessor accessor;
		FormFieldNativeMapping nativeMapping;

		if (propertyClass == String.class) {

			accessor =
				simpleFormFieldAccessorProvider.get ()
					.name (name)
					.nativeClass (String.class);

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else if (propertyClass == TextRec.class) {

			accessor =
				simpleFormFieldAccessorProvider.get ()
					.name (name)
					.nativeClass (TextRec.class);

			nativeMapping =
				textFormFieldNativeMappingProvider.get ();

		} else {

			throw new RuntimeException ();

		}

		// value validator

		FormFieldValueValidator valueValidator =
			nullFormFieldValueValidatorProvider.get ();

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()
				.name (name)
				.label (label)
				.nullable (nullable)
				.size (size);

		// update hook

		FormFieldUpdateHook updateHook =
			simpleFormFieldUpdateHookProvider.get ()
				.name (name);

		// form field

		if (readOnly) {

			formFieldSet.formFields ().add (

				readOnlyFormFieldProvider.get ()

				.name (name)
				.label (label)

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
