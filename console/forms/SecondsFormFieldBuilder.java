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
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("secondsFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class SecondsFormFieldBuilder {

	// prototype dependencies

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<SimpleFormFieldUpdateHook>
	simpleFormFieldUpdateHookProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<NullFormFieldConstraintValidator>
	nullFormFieldConstraintValidatorProvider;

	@Inject
	Provider<NullFormFieldValueValidator>
	nullFormFieldValueValidatorProvider;

	@Inject
	Provider<SecondsFormFieldInterfaceMapping>
	secondsFormFieldInterfaceMapping;

	@Inject
	Provider<SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext formFieldBuilderContext;

	@BuilderSource
	SecondsFormFieldSpec secondsFormFieldSpec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			secondsFormFieldSpec.name ();

		String label =
			ifNull (
				secondsFormFieldSpec.label (),
				capitalise (camelToSpaces (name)));

		Boolean nullable =
			ifNull (
				secondsFormFieldSpec.nullable (),
				false);

		Boolean readOnly =
			ifNull (
				secondsFormFieldSpec.readOnly (),
				false);

		// field components

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				Integer.class);

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldConstraintValidatorProvider.get ();

		FormFieldInterfaceMapping interfaceMapping =
			secondsFormFieldInterfaceMapping.get ()
				.label (label);

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable)

			.size (
				FormField.defaultSize);

		// field

		if (! readOnly) {

			formFieldSet.formFields ().add (
				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					identityFormFieldNativeMappingProvider.get ())

				.valueValidator (
					nullFormFieldValueValidatorProvider.get ())

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					simpleFormFieldUpdateHookProvider.get ()
						.name (name))

			);

		} else {

			formFieldSet.formFields ().add (
				readOnlyFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					identityFormFieldNativeMappingProvider.get ())

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

			);

		}

	}

}
