package wbs.console.forms.basic;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.ifNullThenRequired;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.HiddenFormField;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.object.DereferenceFormFieldAccessor;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.enums.EnumConsoleHelper;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("enumFormFieldBuilder")
public
class EnumFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <DereferenceFormFieldAccessor>
		dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <EnumCsvFormFieldInterfaceMapping>
		enumCsvFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <EnumFormFieldRenderer> enumFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <HiddenFormField> hiddenFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldInterfaceMapping>
		identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldNativeMapping>
		identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <NullFormFieldConstraintValidator>
		nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <RequiredFormFieldValueValidator>
		requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	EnumFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation formFieldSet;

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

			Boolean hidden =
				ifNull (
					spec.hidden (),
					false);

			Class <?> propertyClass =
				optionalGetRequired (
					objectManager.dereferenceType (
						taskLogger,
						optionalOf (
							context.containerClass ()),
						optionalOf (
							fieldName)));

			String enumConsoleHelperName =
				ifNullThenRequired (

				() -> spec.helperBeanName (),

				() -> ifThenElse (
					isNotNull (
						propertyClass.getEnclosingClass ()),

					() -> stringFormat (
						"%s%sConsoleHelper",
						uncapitalise (
							propertyClass.getEnclosingClass ().getSimpleName ()),
						propertyClass.getSimpleName ()),

					() -> stringFormat (
						"%sConsoleHelper",
						uncapitalise (
							propertyClass.getSimpleName ()))

				)

			);

			EnumConsoleHelper enumConsoleHelper =
				componentManager.getComponentOrElse (
					taskLogger,
					enumConsoleHelperName,
					EnumConsoleHelper.class,
					() -> new EnumConsoleHelper ()
						.enumClass (propertyClass)
						.auto ());

			Optional <Optional <Object>> implicitValue =
				spec.implicitValue () != null
					? Optional.of (
						Optional.of (
							toEnum (
								enumConsoleHelper.enumClass (),
								spec.implicitValue ())))
					: Optional.absent ();

			// accessor

			FormFieldAccessor accessor =
				dereferenceFormFieldAccessorProvider.provide (
					taskLogger)

				.path (
					fieldName)

				.nativeClass (
					enumConsoleHelper.enumClass ());

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				identityFormFieldNativeMappingProvider.provide (
					taskLogger);

			// value validators

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.provide (
						taskLogger));

			}

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nullFormFieldValueConstraintValidatorProvider.provide (
					taskLogger);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				identityFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// csv mapping

			FormFieldInterfaceMapping csvMapping =
				enumCsvFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// renderer

			FormFieldRenderer renderer =
				enumFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.nullable (
					nullable)

				.enumConsoleHelper (
					enumConsoleHelper);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// form field

			if (hidden) {

				formFieldSet.addFormItem (
					hiddenFormFieldProvider.provide (
						taskLogger)

					.name (
						name)

					.accessor (
						accessor)

					.nativeMapping (
						nativeMapping)

					.csvMapping (
						csvMapping)

					.implicitValue (
						implicitValue)

				);

			} else if (readOnly) {

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

				);

			} else {

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

				);

			}

		}

	}

}
