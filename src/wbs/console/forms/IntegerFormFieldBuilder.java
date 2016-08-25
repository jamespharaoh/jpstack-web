package wbs.console.forms;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("integerFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class IntegerFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManager formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider <DynamicFormFieldAccessor>
	dynamicFormFieldAccessorProvider;

	@Inject
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider <IntegerFormFieldInterfaceMapping>
	integerFormFieldInterfaceMappingProvider;

	@Inject
	Provider <IntegerFormFieldValueValidator>
	integerFormFieldValueValidatorProvider;

	@Inject
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@Inject
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@Inject
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	IntegerFormFieldSpec spec;

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

		String fullName =
			spec.delegate () == null
				? name
				: stringFormat (
					"%s.%s",
					spec.delegate (),
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

		Boolean dynamic =
			ifNull (
				spec.dynamic (),
				false);

		Class<?> propertyClass =
			dynamic
				? Long.class
				: BeanLogic.propertyClassForClass (
					context.containerClass (),
					name);

		Boolean blankIfZero =
			ifNull (
				spec.blankIfZero (),
				false);

		// accessor

		FormFieldAccessor accessor;

		if (dynamic) {

			accessor =
				dynamicFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

		}

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

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

		FormFieldInterfaceMapping interfaceMapping =
			integerFormFieldInterfaceMappingProvider.get ()

			.blankIfZero (
				blankIfZero);

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				fullName)

			.label (
				label)

			.nullable (
				ifNull (
					spec.nullable (),
					false));

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
					fullName)

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

			formFieldSet.addFormField (
				readOnlyFormFieldProvider.get ()

				.name (
					fullName)

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
