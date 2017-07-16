package wbs.console.forms.object;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.core.ConsoleHelper;
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
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("objectFormFieldBuilder")
public
class ObjectFormFieldBuilder
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
	ComponentProvider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <DynamicFormFieldAccessor>
	dynamicFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <ObjectCsvFormFieldInterfaceMapping>
	objectCsvFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <ObjectFormFieldRenderer>
	objectFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <ObjectIdFormFieldNativeMapping>
	objectIdFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <ObjectFormFieldConstraintValidator>
	objectFormFieldConstraintValidatorProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	ObjectFormFieldSpec spec;

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
							name.endsWith ("Id")
								? name.substring (
									0,
									name.length () - 2)
								: name)));

			Boolean nullable =
				ifNull (
					spec.nullable (),
					false);

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					false);

			Boolean dynamic =
				ifNull (
					spec.dynamic (),
					false);

			Optional <ConsoleHelper <?>> consoleHelperOptional;

			if (
				isNotNull (
					spec.objectTypeName ())
			) {

				consoleHelperOptional =
					optionalOf (
						objectManager.consoleHelperForNameRequired (
							spec.objectTypeName ()));

				if (
					optionalIsNotPresent (
						consoleHelperOptional)
				) {

					throw new RuntimeException (
						stringFormat (
							"Console helper does not exist: %s",
							spec.objectTypeName ()));

				}

			} else {

				consoleHelperOptional =
					optionalAbsent ();

			}

			String rootFieldName =
				spec.rootFieldName ();

			// field type

			Optional <Class <?>> propertyClassOptional;

			if (dynamic) {

				if (
					optionalIsPresent (
						consoleHelperOptional)
				) {

					ConsoleHelper <?> consoleHelper =
						optionalGetRequired (
							consoleHelperOptional);

					propertyClassOptional =
						optionalOf (
							consoleHelper.objectClass ());

				} else {

					propertyClassOptional =
						optionalAbsent ();

				}

			} else {

				propertyClassOptional =
					objectManager.dereferenceType (
						taskLogger,
						optionalOf (
							context.containerClass ()),
						optionalOf (
							fieldName));

			}

			// accessor

			FormFieldAccessor accessor;

			if (dynamic) {

				accessor =
					dynamicFormFieldAccessorProvider.provide (
						taskLogger)

					.name (
						fieldName)

					.nativeClass (
						optionalOrNull (
							propertyClassOptional))

				;

			} else if (
				isNotNull (
					spec.fieldName ())
			) {

				accessor =
					dereferenceFormFieldAccessorProvider.provide (
						taskLogger)

					.path (
						spec.fieldName ());

			} else {

				accessor =
					simpleFormFieldAccessorProvider.provide (
						taskLogger)

					.name (
						name)

					.nativeClass (
						propertyClassOptional.get ());

			}

			// native mapping

			ConsoleFormNativeMapping nativeMapping;

			if (
				optionalValueEqualSafe (
					propertyClassOptional,
					Long.class)
			) {

				nativeMapping =
					objectIdFormFieldNativeMappingProvider.provide (
						taskLogger)

					.consoleHelper (
						consoleHelperOptional.orNull ());

			} else {

				nativeMapping =
					identityFormFieldNativeMappingProvider.provide (
						taskLogger);

			}

			// value validator

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.provide (
						taskLogger));

			}

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				objectFormFieldConstraintValidatorProvider.provide (
					taskLogger);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				identityFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// csv mapping

			FormFieldInterfaceMapping csvMapping =
				objectCsvFormFieldInterfaceMappingProvider.provide (
					taskLogger)

				.rootFieldName (
					rootFieldName);

			// renderer

			FormFieldRenderer renderer =
				objectFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.nullable (
					nullable)

				.rootFieldName (
					rootFieldName)

				.entityFinder (
					consoleHelperOptional.orNull ())

				.mini (
					isNotNull (
						spec.objectTypeName ()));

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// field

			if (! readOnly) {

				formFieldSet.addFormItem (
					updatableFormFieldProvider.provide (
						taskLogger)

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
						csvMapping)

					.renderer (
						renderer)

					.updateHook (
						updateHook)

					.viewPriv (
						spec.viewPrivCode ())

					.featureCode (
						spec.featureCode ())

				);

			} else {

				formFieldSet.addFormItem (
					readOnlyFormFieldProvider.provide (
						taskLogger)

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
						csvMapping)

					.renderer (
						renderer)

					.viewPriv (
						spec.viewPrivCode ())

					.featureCode (
						spec.featureCode ())

				);

			}

		}

	}

}
