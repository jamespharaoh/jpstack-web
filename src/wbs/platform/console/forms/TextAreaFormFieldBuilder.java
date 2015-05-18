package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.etc.BeanLogic;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.text.model.TextRec;
import wbs.services.messagetemplate.console.FormFieldDataProvider;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("textAreaFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TextAreaFormFieldBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	// prototype dependencies

	@Inject
	Provider<SimpleFormFieldUpdateHook>
	simpleFormFieldUpdateHookProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

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
	Provider<TextAreaFormFieldRenderer>
	textAreaFormFieldRendererProvider;

	@Inject
	Provider<TextFormFieldNativeMapping>
	textFormFieldNativeMappingProvider;
	
	FormFieldDataProvider formFielDataProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext formFieldBuilderContext;

	@BuilderSource
	TextAreaFormFieldSpec textAreaFormFieldSpec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			textAreaFormFieldSpec.name ();

		String label =
			ifNull (
				textAreaFormFieldSpec.label (),
				capitalise (camelToSpaces (name)));

		Boolean readOnly =
			ifNull (
				textAreaFormFieldSpec.readOnly (),
				false);

		Boolean nullable =
			ifNull (
				textAreaFormFieldSpec.nullable (),
				false);

		Integer rows =
			ifNull (
				textAreaFormFieldSpec.rows (),
				4);

		Integer cols =
			ifNull (
				textAreaFormFieldSpec.cols (),
				FormField.defaultSize);

		String charCountFunction =
			textAreaFormFieldSpec.charCountFunction ();

		String charCountData =
			textAreaFormFieldSpec.charCountData ();
		
		// if a data provider is provided
		
		Class<?> propertyClass = null;
		
		if (textAreaFormFieldSpec.dataProvider () != null) {
			
			propertyClass = String.class;

			formFielDataProvider = 
				applicationContext.getBean (
					textAreaFormFieldSpec.dataProvider (),
					FormFieldDataProvider.class);		
		}
		else {
			// field type
	
			propertyClass =
				BeanLogic.propertyClass (
					formFieldBuilderContext.containerClass (),
					name);
		}

		String updateHookBeanName =
			textAreaFormFieldSpec.updateHookBeanName ();

		FormFieldAccessor formFieldAccessor;
		FormFieldNativeMapping formFieldNativeMapping;

		if (propertyClass == String.class) {

			formFieldAccessor =
				simpleFormFieldAccessorProvider.get ()
					.name (name)
					.nativeClass (String.class);

			formFieldNativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else if (propertyClass == TextRec.class) {

			formFieldAccessor =
				simpleFormFieldAccessorProvider.get ()
					.name (name)
					.nativeClass (TextRec.class);

			formFieldNativeMapping =
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

		FormFieldRenderer formFieldRenderer =
			textAreaFormFieldRendererProvider.get ()
				.name (name)
				.label (label)
				.nullable (nullable)
				.rows (rows)
				.cols (cols)
				.charCountFunction (charCountFunction)
				.charCountData (charCountData)
				.formFieldDataProvider (formFielDataProvider);

		// update hook

		FormFieldUpdateHook formFieldUpdateHook =
			updateHookBeanName != null
				? applicationContext.getBean (
					updateHookBeanName,
					FormFieldUpdateHook.class)
				: simpleFormFieldUpdateHookProvider.get ()
					.name (name);

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
					formFieldAccessor)

				.nativeMapping (
					formFieldNativeMapping)

				.interfaceMapping (
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
					formFieldAccessor)

				.nativeMapping (
					formFieldNativeMapping)

				.valueValidator (
					valueValidator)

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					formFieldRenderer)

				.updateHook (
					formFieldUpdateHook)

			);

		}

	}

}
