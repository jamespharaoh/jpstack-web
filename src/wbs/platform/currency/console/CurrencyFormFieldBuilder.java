package wbs.platform.currency.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.apache.commons.lang3.Range;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.DereferenceFormFieldAccessor;
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
import wbs.console.forms.IntegerFormFieldValueValidator;
import wbs.console.forms.NullFormFieldConstraintValidator;
import wbs.console.forms.RangeFormFieldInterfaceMapping;
import wbs.console.forms.ReadOnlyFormField;
import wbs.console.forms.RequiredFormFieldValueValidator;
import wbs.console.forms.TextFormFieldRenderer;
import wbs.console.forms.TextualRangeFormFieldInterfaceMapping;
import wbs.console.forms.UpdatableFormField;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("currencyFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class CurrencyFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <CurrencyFormFieldInterfaceMapping>
	currencyFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <IntegerFormFieldInterfaceMapping>
	integerFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <IntegerFormFieldValueValidator>
	integerFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <RangeFormFieldInterfaceMapping>
	rangeFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <TextualRangeFormFieldInterfaceMapping>
	textualRangeFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

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

		String fieldName =
			ifNull (
				spec.fieldName (),
				name);

		String label =
			ifNull (
				spec.label (),
				capitalise (
					camelToSpaces (
						name)));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		Boolean nullable =
			ifNull (
				spec.nullable (),
				false);

		Long minimum =
			ifNull (
				spec.minimum (),
				Long.MIN_VALUE);

		Long maximum =
			ifNull (
				spec.maximum (),
				Long.MAX_VALUE);

		Class <?> propertyClass =
			optionalGetRequired (
				objectManager.dereferenceType (
					optionalOf (
						context.containerClass ()),
					optionalOf (
						fieldName)));

		Boolean blankIfZero =
			ifNull (
				spec.blankIfZero (),
				false);

		// accessor

		FormFieldAccessor accessor =
			dereferenceFormFieldAccessorProvider.get ()

			.path (
				fieldName)

			.nativeClass (
				propertyClass);

		// native mapping

		FormFieldNativeMapping nativeMapping;

		boolean range;

		if (propertyClass == Long.class) {

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

			range = false;

		} else if (propertyClass == Range.class) {

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

			range = true;

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map %s as integer for %s.%s",
					propertyClass,
					context.containerClass (),
					name));

		}

		// value validator

		List <FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		if (! nullable) {

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

		}

		valueValidators.add (
			integerFormFieldValueValidatorProvider.get ()

			.label (
				label)

			.minimum (
				minimum)

			.maximum (
				maximum)

		);

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping;

		if (range) {

			interfaceMapping =
				rangeFormFieldInterfaceMappingProvider.get ()

				.itemMapping (
					currencyFormFieldInterfaceMappingProvider.get ()

					.currencyPath (
						spec.currencyPath ())

					.blankIfZero (
						blankIfZero));

		} else {

			interfaceMapping =
				currencyFormFieldInterfaceMappingProvider.get ()

				.currencyPath (
					spec.currencyPath ())

				.blankIfZero (
					blankIfZero);

		}

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				ifNull (
					spec.nullable (),
					false))

			.listAlign (
				FormField.Align.right);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (! readOnly) {

			formFieldSet.addFormItem (
				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.valueValidators (
					valueValidators)

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

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

			);

		}

	}

}
