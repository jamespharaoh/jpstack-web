package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("textAreaFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TextAreaFormFieldBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<DynamicFormFieldAccessor>
	dynamicFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

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
	Provider<TextAreaFormFieldRenderer>
	textAreaFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	TextAreaFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// state

	FormFieldDataProvider dataProvider;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

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

		Boolean nullable =
			ifNull (
				spec.nullable (),
				false);

		Integer rows =
			ifNull (
				spec.rows (),
				4);

		Integer cols =
			ifNull (
				spec.cols (),
				FormField.defaultSize);

		Boolean dynamic =
			spec.dynamic ();

		Record<?> parent =
			spec.parent ();

		String charCountFunction =
			spec.charCountFunction ();

		String charCountData =
			spec.charCountData ();

		// if a data provider is provided

		Class<?> propertyClass = null;

		if (spec.dataProvider () != null) {

			propertyClass =
				String.class;

			dataProvider =
				applicationContext.getBean (
					spec.dataProvider (),
					FormFieldDataProvider.class);

		} else if (! dynamic) {

			propertyClass =
				BeanLogic.propertyClassForClass (
					context.containerClass (),
					name);

		} else {

			propertyClass =
				String.class;

		}

		String updateHookBeanName =
			spec.updateHookBeanName ();

		// constraint validator only use for simple type settings

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

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

		// TODO dynamic

		// native mapping

		FormFieldNativeMapping<?,?> nativeMapping =
			formFieldPluginManager.getNativeMapping (
				context,
				context.containerClass (),
				name,
				String.class,
				propertyClass);

		// value validator

		FormFieldValueValidator valueValidator =
			nullFormFieldValueValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer formFieldRenderer =
			textAreaFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable)

			.rows (
				rows)

			.cols (
				cols)

			.charCountFunction (
				charCountFunction)

			.charCountData (
				charCountData)

			.parent (
				parent)

			.formFieldDataProvider (
				dataProvider);

		// update hook

		FormFieldUpdateHook formFieldUpdateHook =
			updateHookBeanName != null

			? applicationContext.getBean (
				updateHookBeanName,
				FormFieldUpdateHook.class)

			: formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (readOnly) {

			formFieldSet.formFields ().add (

				readOnlyFormFieldProvider.get ()

				.large (
					true)

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
					formFieldRenderer)

			);

		} else {

			formFieldSet.formFields ().add (

				updatableFormFieldProvider.get ()

				.large (
					true)

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
					formFieldRenderer)

				.updateHook (
					formFieldUpdateHook)

			);

		}

	}

}
