package wbs.console.forms.upload;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.NullFormFieldConstraintValidator;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManager;
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
@PrototypeComponent ("uploadFormFieldBuilder")
public
class UploadFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormPluginManager formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

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
	ComponentProvider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <UploadFormFieldRenderer> uploadFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	UploadFormFieldSpec spec;

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

			String label =
				ifNull (
					spec.label (),
					capitalise (
						camelToSpaces (
							name)));

			Boolean nullable =
				ifNull (
					spec.nullable (),
					false);

			// accessor

			FormFieldAccessor accessor =
				simpleFormFieldAccessorProvider.provide (
					taskLogger)

				.name (
					name)

				.nativeClass (
					FileUpload.class);

			ConsoleFormNativeMapping nativeMapping =
				identityFormFieldNativeMappingProvider.provide (
					taskLogger);

			// value validators

			List<FormFieldValueValidator> valueValidators =
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

			// renderer

			FormFieldRenderer renderer =
				uploadFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// form field

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
