package wbs.console.forms;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("textAreaFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TextAreaFormFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <DynamicFormFieldAccessor>
	dynamicFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextAreaFormFieldRenderer>
	textAreaFormFieldRendererProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	@PrototypeDependency
	Provider <Utf8StringFormFieldNativeMapping>
	utf8StringFormFieldNativeMappingProvider;

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
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String name =
				spec.name ();

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
					componentManager.getComponentRequired (
						taskLogger,
						spec.dataProvider (),
						FormFieldDataProvider.class);

			} else if (! dynamic) {

				propertyClass =
					PropertyUtils.propertyClassForClass (
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

			FormFieldNativeMapping nativeMapping;

			if (
				classEqualSafe (
					propertyClass,
					byte[].class)
			) {

				nativeMapping =
					utf8StringFormFieldNativeMappingProvider.get ();


			} else {

				nativeMapping =
					formFieldPluginManager.getNativeMappingRequired (
						context,
						context.containerClass (),
						name,
						String.class,
						propertyClass);

			}

			// value validator

			List<FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.get ());

			}

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

				? componentManager.getComponentRequired (
					taskLogger,
					updateHookBeanName,
					FormFieldUpdateHook.class)

				: formFieldPluginManager.getUpdateHook (
					context,
					context.containerClass (),
					name);

			// field

			if (readOnly) {

				formFieldSet.addFormItem (

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

				formFieldSet.addFormItem (

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

					.valueValidators (
						valueValidators)

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

}
