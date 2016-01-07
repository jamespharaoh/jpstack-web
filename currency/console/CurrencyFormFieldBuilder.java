package wbs.platform.currency.console;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.FormField;
import wbs.console.forms.FormFieldAccessor;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldConstraintValidator;
import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.forms.FormFieldPluginManagerImplementation;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.forms.FormFieldValueValidator;
import wbs.console.forms.IdentityFormFieldNativeMapping;
import wbs.console.forms.IntegerFormFieldInterfaceMapping;
import wbs.console.forms.IntegerFormFieldNativeMapping;
import wbs.console.forms.IntegerFormFieldValueValidator;
import wbs.console.forms.NullFormFieldConstraintValidator;
import wbs.console.forms.ReadOnlyFormField;
import wbs.console.forms.SimpleFormFieldAccessor;
import wbs.console.forms.TextFormFieldRenderer;
import wbs.console.forms.UpdatableFormField;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("currencyFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class CurrencyFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

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
	Provider<IntegerFormFieldInterfaceMapping>
	integerFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IntegerFormFieldNativeMapping>
	integerFormFieldNativeMappingProvider;

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

		Long minimum =
			ifNull (
				spec.minimum (),
				Long.MIN_VALUE);

		Long maximum =
			ifNull (
				spec.maximum (),
				Long.MAX_VALUE);

		Class<?> propertyClass =
			BeanLogic.propertyClassForClass (
				context.containerClass (),
				name);

		Boolean blankIfZero =
			ifNull (
				spec.blankIfZero (),
				false);

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				propertyClass);

		// native mapping

		FormFieldNativeMapping nativeMapping ;


		if (propertyClass == Integer.class) {

			nativeMapping =
				integerFormFieldNativeMappingProvider.get ();

		} else if (propertyClass == Long.class) {

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map %s as integer for %s.%s",
					propertyClass,
					context.containerClass (),
					name));

		}

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
				spec.currencyPath ())

			.blankIfZero (
				blankIfZero);

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
					false))

			.align (
				TextFormFieldRenderer.Align.right);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (! readOnly) {

			formFieldSet.addFormField (
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

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		} else {

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

}
