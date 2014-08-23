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
@PrototypeComponent ("currencyFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class CurrencyFormFieldBuilder {

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
	nullFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<IntegerFormFieldValueValidator>
	integerFormFieldValueValidatorProvider;

	@Inject
	Provider<SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider<CurrencyFormFieldInterfaceMapping>
	currencyFormFieldInterfaceMappingProvider;

	@Inject
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	CurrencyFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// resolve properties from spec

		String name =
			spec.name ();

		String label =
			ifNull (
				spec.label (),
				capitalise (camelToSpaces (name)));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		Integer minimum =
			ifNull (
				spec.minimum (),
				Integer.MIN_VALUE);

		Integer maximum =
			ifNull (
				spec.maximum (),
				Integer.MAX_VALUE);

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				Integer.class);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// value validator

		FormFieldValueValidator valueValidator =
			integerFormFieldValueValidatorProvider.get ()

			.label (
				label)

			.minimum (
				minimum)

			.maximum (
				maximum);

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			currencyFormFieldInterfaceMappingProvider.get ()

			.currencyPath (
				spec.currencyPath ());

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.size (
				FormField.defaultSize)

			.nullable (
				ifNull (
					spec.nullable (),
					false));

		// update hook

		FormFieldUpdateHook updateHook =
			simpleFormFieldUpdateHookProvider.get ()

			.name (
				name);

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
					nativeMapping)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

			);

		}

	}

}
