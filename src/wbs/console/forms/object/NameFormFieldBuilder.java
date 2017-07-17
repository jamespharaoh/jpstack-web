package wbs.console.forms.object;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.ifNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.text.TextFormFieldRenderer;
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
@PrototypeComponent ("nameFormFieldBuilder")
public
class NameFormFieldBuilder
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
	ComponentProvider <DelegateFormFieldAccessor>
		delegateFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldInterfaceMapping>
		identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <NameFormFieldAccessor> nameFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldNativeMapping>
		identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <NameFormFieldConstraintValidator>
		nameFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	ComponentProvider <NameFormFieldValueValidator>
		nameFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <RequiredFormFieldValueValidator>
		requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <TextFormFieldRenderer> textFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	NameFormFieldSpec spec;

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

			ConsoleHelper consoleHelper =
				context.consoleHelper ();

			String name =
				ifNull (
					spec.name (),
					"name");

			String label =
				ifNull (
					spec.label (),
					"Name");

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					false);

			// accessor

			FormFieldAccessor accessor =
				nameFormFieldAccessorProvider.provide (
					taskLogger)

				.consoleHelper (
					consoleHelper);

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				identityFormFieldNativeMappingProvider.provide (
					taskLogger);

			// value validator

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.provide (
					taskLogger));

			valueValidators.add (
				nameFormFieldValueValidatorProvider.provide (
					taskLogger)

				.namePattern (
					ifNotNullThenElse (
						spec.pattern (),
						() -> Pattern.compile (
							spec.pattern ()),
						() -> null))

				.nameError (
					spec.patternError ())

				.codePattern (
					ifNotNullThenElse (
						spec.codePattern (),
						() -> Pattern.compile (
							spec.codePattern ()),
						() -> null))

			);

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nameFormFieldValueConstraintValidatorProvider.provide (
					taskLogger);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				identityFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// renderer

			FormFieldRenderer renderer =
				textFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.nullable (
					false);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// field

			if (readOnly) {

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

					.renderer (
						renderer)

					.updateHook (
						updateHook)

				);

			}

		}

	}

}
