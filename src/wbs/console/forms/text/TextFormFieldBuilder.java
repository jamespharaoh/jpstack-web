package wbs.console.forms.text;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.NullFormFieldConstraintValidator;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.HiddenFormField;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.object.DereferenceFormFieldAccessor;
import wbs.console.forms.object.DynamicFormFieldAccessor;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("textFormFieldBuilder")
public
class TextFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <DynamicFormFieldAccessor> dynamicFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <HiddenFormField> hiddenFormFieldProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer> textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <TextFormFieldValueValidator> textFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	TextFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation formFieldSet;

	// build

	@Override
	@BuildMethod
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

			Boolean hidden =
				ifNull (
					spec.hidden (),
					false);

			Boolean nullable =
				ifNull (
					spec.nullable (),
					false);

			Boolean dynamic =
				ifNull (
					spec.dynamic (),
					false);

			// field type

			Class <?> propertyClass;

			if (! dynamic) {

				propertyClass =
					optionalGetRequired (
						objectManager.dereferenceType (
							taskLogger,
							optionalOf (
								context.containerClass ()),
							optionalOf (
								fieldName)));

			} else {

				propertyClass =
					String.class;

			}

			// accessor

			FormFieldAccessor accessor;

			if (dynamic) {

				accessor =
					dynamicFormFieldAccessorProvider.get ()

					.name (
						name)

					.nativeClass (
						propertyClass);

			} else if (
				isNotNull (
					spec.fieldName ())
			) {

				accessor =
					dereferenceFormFieldAccessorProvider.get ()

					.path (
						fieldName);

			} else {

				accessor =
					simpleFormFieldAccessorProvider.get ()

					.name (
						fieldName)

					.nativeClass (
						propertyClass);

			}

			// TODO dynamic

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				formFieldPluginManager.getNativeMappingRequired (
					context,
					context.containerClass (),
					name,
					String.class,
					propertyClass);

			// value validator

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.get ());

			}

			valueValidators.add (
				textFormFieldValueValidatorProvider.get ()

				.trim (
					true)

				.minimumLength (
					spec.minimumLength ())

				.maximumLength (
					spec.maximumLength ())

			);

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nullFormFieldValueConstraintValidatorProvider.get ();

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
					nullable);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					context,
					context.containerClass (),
					name);

			// form field

			if (hidden) {

				formFieldSet.addFormItem (
					hiddenFormFieldProvider.get ()

					.name (
						name)

					.accessor (
						accessor)

					.nativeMapping (
						nativeMapping)

					.csvMapping (
						interfaceMapping)

					.implicitValue (
						Optional.absent ())

				);

			} else if (readOnly) {

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

			} else {

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

			}

		}

	}

}
